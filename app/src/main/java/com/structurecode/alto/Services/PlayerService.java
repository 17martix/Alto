package com.structurecode.alto.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.structurecode.alto.Download.SongDownloadManager;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import java.util.ArrayList;

public class PlayerService extends Service {
    private final IBinder iBinder = new LocalBinder();

    public static final String ADD_TO_QUEUE="com.structurecode.alto.services.player.add.queue";

    public static final String AUDIO_EXTRA="song_extra";

    private PlayerNotificationManager playerNotificationManager;
    private static final String PLAYBACK_CHANNEL_ID="com.structurecode.alto.services.player.chanel.id";
    private static final int PLAYBACK_NOTIFICATION_ID=2;
    private static final String MEDIA_SESSION_TAG="com.structurecode.alto.services.player.media.session";

    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;

    private Context context;
    private ArrayList<Song> playlist;

    private BroadcastReceiver add_queue_receiver;

    private SongDownloadManager songDownloadManager;

    public SimpleExoPlayer player;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private DataSource.Factory dataSourceFactory;
    private CacheDataSourceFactory cacheDataSourceFactory;
    private DefaultTrackSelector trackSelector;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private TrackGroupArray lastSeenTrackGroupArray;

    @Override
    public void onCreate() {
        super.onCreate();

        context=this;
        playlist=new ArrayList<>();
        songDownloadManager = new SongDownloadManager(this);

        dataSourceFactory = songDownloadManager.buildDataSourceFactory();

        /*cacheDataSourceFactory =
                new CacheDataSourceFactory(AudioDownloadUtil.getCache(context), dataSourceFactory,
                        new FileDataSource.Factory(),
                        new CacheDataSinkFactory(AudioDownloadUtil.getCache(context), CacheDataSink.DEFAULT_FRAGMENT_SIZE),
                        0,null,new CacheKeyProvider());*/

        DefaultTrackSelector.ParametersBuilder builder =
                new DefaultTrackSelector.ParametersBuilder(/* context= */ this);
        trackSelectorParameters = builder.build();
        TrackSelection.Factory trackSelectionFactory;
        trackSelectionFactory = new RandomTrackSelection.Factory();
        RenderersFactory renderersFactory =
                songDownloadManager.buildRenderersFactory(false);

        trackSelector = new DefaultTrackSelector(/* context= */ this, trackSelectionFactory);
        trackSelector.setParameters(trackSelectorParameters);
        lastSeenTrackGroupArray = null;

        concatenatingMediaSource=new ConcatenatingMediaSource();

        LoadControl loadControl=new DefaultLoadControl.Builder().setBufferDurationsMs(300000,600000,
                1000,3000).createDefaultLoadControl();
        /*player=new SimpleExoPlayer.Builder(context)
                .setLoadControl(loadControl)
                .build();*/

        player = new SimpleExoPlayer.Builder(/* context= */ this, renderersFactory)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .build();
        player.addAnalyticsListener(new EventLogger(trackSelector));

        initialize_broadcasts();

    }
}
