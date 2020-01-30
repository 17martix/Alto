package com.structurecode.alto.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.structurecode.alto.Download.SongDownloadManager;
import com.structurecode.alto.Download.SongDownloadTracker;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import java.util.ArrayList;
import java.util.List;

public class PlayerService extends Service implements SongDownloadTracker.Listener {
    public static final String ADD_TO_QUEUE="com.structurecode.alto.services.player.add.queue";
    public static final String DOWNLOAD_SONG="com.structurecode.alto.services.player.download.song";
    public static final String PLAY_SONG="com.structurecode.alto.services.player.play.song";
    public static final String AUDIO_EXTRA="song_extra";
    public static final String AUDIO_LIST_EXTRA="song_list_extra";

    private static final String PLAYBACK_CHANNEL_ID="com.structurecode.alto.services.player.chanel.id.alto";
    private static final int PLAYBACK_CHANNEL_NAME=4;
    private static final int PLAYBACK_CHANNEL_DESCRIPTION=3;
    private static final int PLAYBACK_NOTIFICATION_ID=2;
    private static final String MEDIA_SESSION_TAG="com.structurecode.alto.services.player.media.session";

    private static final String DOWNLOAD_CHANGED = "com.structurecode.alto.services.player.download.changed";

    private final IBinder iBinder = new LocalBinder();

    private PlayerNotificationManager playerNotificationManager;
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;

    private Context context;
    private ArrayList<Song> playlist;

    private BroadcastReceiver add_queue_receiver;
    private BroadcastReceiver download_song_receiver;
    private BroadcastReceiver play_song_receiver;

    private SongDownloadManager songDownloadManager;

    public SimpleExoPlayer player;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private DataSource.Factory dataSourceFactory;
    private SongDownloadTracker downloadTracker;

    private MediaMetadataRetriever mediaMetadataRetriever;
    private Bitmap albumImage=null;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();

        context=this;
        playlist=new ArrayList<>();
        songDownloadManager = new SongDownloadManager(this);
        mediaMetadataRetriever = new MediaMetadataRetriever();

        dataSourceFactory = songDownloadManager.buildDataSourceFactory();
        downloadTracker = songDownloadManager.getDownloadTracker();
        downloadTracker.addListener(this);
        concatenatingMediaSource=new ConcatenatingMediaSource();

        TrackSelector trackSelector=new DefaultTrackSelector(context);
        RenderersFactory renderersFactory = new DefaultRenderersFactory(context)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);

        LoadControl loadControl=new DefaultLoadControl.Builder().setBufferDurationsMs(300000,600000,
                1000,3000).createDefaultLoadControl();

        player = new SimpleExoPlayer.Builder(/* context= */ this, renderersFactory)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .build();

        initialize_broadcasts();
        notification_manager();
        addPlayerListener();
        deviceCheck();

        // Start the download service if it should be running but it's not currently.
        // Starting the service in the foreground causes notification flicker if there is no scheduled
        // action. Starting it in the background throws an exception if the app is in the background too
        // (e.g. if device screen is locked).
        try {
            DownloadService.start(this, SongDownloadService.class);
        } catch (IllegalStateException e) {
            DownloadService.startForeground(this, SongDownloadService.class);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return  START_STICKY;
    }

    @Override
    public void onDownloadsChanged() {
        Intent intent=new Intent();
        intent.setAction(DOWNLOAD_CHANGED);
        sendBroadcast(intent);
    }

    public void deviceCheck(){
        mAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        final DocumentReference docRef = db.collection(Utils.COLLECTION_USERS).document(user.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("ABC", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {

                    String device_id = snapshot.getString("device_id");

                    if (!device_id.equals(Utils.get_device_id(context))){
                        Intent intent=new Intent();
                        intent.setAction(Utils.DEVICE_CHECK);
                        sendBroadcast(intent);
                    }

                } else {
                    Log.d("ABC", "Current data: null");
                }
            }
        });
    }

    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    public String GetPlayingInfo(){
        String result="";
        if (playlist.size()>0) {
            String title = playlist.get(player.getCurrentWindowIndex()).getTitle();
            String artist = playlist.get(player.getCurrentWindowIndex()).getArtist();
            result = artist + " - " + title;
        }
        return result;
    }

    public void play_song(Song song, List<Song> songList){
        if (playlist.size()>0){
            playlist.clear();
        }
        MediaSource[] mediaSources = new ProgressiveMediaSource[songList.size()];
        for (int i=0; i<mediaSources.length;i++){
            MediaSource mediaSource = getMedia(songList.get(i));
            mediaSources[i]=mediaSource;

            playlist.add(songList.get(i));
        }
        concatenatingMediaSource=new ConcatenatingMediaSource(mediaSources);

        int index=songList.indexOf(song);
        player.prepare(concatenatingMediaSource);
        player.seekTo(index,C.TIME_UNSET);
        player.setPlayWhenReady(true);

    }

    public MediaSource getMedia(Song song){
        DownloadRequest downloadRequest = songDownloadManager.getDownloadTracker().getDownloadRequest(Uri.parse(song.getUrl()));
        if (downloadRequest != null) {
            return DownloadHelper.createMediaSource(downloadRequest, dataSourceFactory);
        }

        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(song.getUrl()));
    }

    public void notification_manager(){
        playerNotificationManager=PlayerNotificationManager.createWithNotificationChannel(context, PLAYBACK_CHANNEL_ID, PLAYBACK_CHANNEL_NAME,
                PLAYBACK_CHANNEL_DESCRIPTION, PLAYBACK_NOTIFICATION_ID, new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public String getCurrentContentTitle(Player player) {
                        return playlist.get(player.getCurrentWindowIndex()).getTitle();
                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        Intent intent = new Intent(context, PlayerService.class);
                        return null;
                    }

                    @Nullable
                    @Override
                    public String getCurrentContentText(Player player) {
                        return playlist.get(player.getCurrentWindowIndex()).getArtist();
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        return getAlbumImage(playlist.get(player.getCurrentWindowIndex()));
                    }
                }, new PlayerNotificationManager.NotificationListener() {
                    @Override
                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                        stopSelf();
                    }

                    @Override
                    public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                        if (ongoing) {
                            // Make sure the service will not get destroyed while playing media.
                            startForeground(notificationId, notification);
                        } else {
                            // Make notification cancellable.
                            stopForeground(false);
                        }
                    }
                });
        playerNotificationManager.setPlayer(player);
        playerNotificationManager.setFastForwardIncrementMs(0);
        playerNotificationManager.setRewindIncrementMs(0);
        playerNotificationManager.setUseStopAction(false);
        playerNotificationManager.setSmallIcon(R.drawable.mini_play);
        playerNotificationManager.setUseNavigationActions(true);
        playerNotificationManager.setUseNavigationActionsInCompactView(true);

        mediaSession = new MediaSessionCompat(context, MEDIA_SESSION_TAG);
        mediaSession.setActive(true);
        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());

        mediaSessionConnector=new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSession) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                return new MediaDescriptionCompat.Builder()
                        .setMediaId(""+playlist.get(windowIndex).getId())
                        .setTitle(""+playlist.get(windowIndex).getTitle())
                        .setDescription(""+playlist.get(windowIndex).getArtist())
                        .setIconBitmap(albumImage)
                        .build();
            }
        });
        mediaSessionConnector.setPlayer(player);


    }

    public void initialize_broadcasts(){
        IntentFilter add_queue_filter = new IntentFilter();
        add_queue_filter.addAction(ADD_TO_QUEUE);
        add_queue_receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song song=(Song)intent.getParcelableExtra(AUDIO_EXTRA);
                add_to_queue(song);
            }
        };

        IntentFilter download_song_filter = new IntentFilter();
        download_song_filter.addAction(DOWNLOAD_SONG);
        download_song_receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song song=(Song)intent.getParcelableExtra(AUDIO_EXTRA);
                downloadTracker.toggleDownload(song.getUrl(),Uri.parse(song.getUrl()));

            }
        };

        IntentFilter play_song_filter = new IntentFilter();
        play_song_filter.addAction(PLAY_SONG);
        play_song_receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song song=(Song)intent.getParcelableExtra(AUDIO_EXTRA);
                ArrayList<Song> list = intent.getParcelableArrayListExtra(AUDIO_LIST_EXTRA);
                play_song(song,list);

            }
        };

        registerReceiver(play_song_receiver,play_song_filter);
        registerReceiver(download_song_receiver,download_song_filter);
        registerReceiver(add_queue_receiver,add_queue_filter);
    }

    public void add_to_queue(Song song){
        MediaSource progressiveMediaSource = getMedia(song);
        playlist.add(song);
        concatenatingMediaSource.addMediaSource(progressiveMediaSource);
    }

    public Bitmap getAlbumImage(Song song){

        mediaMetadataRetriever.setDataSource(context,Uri.parse(song.getUrl()));
        //String albumName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        byte[] artBytes =  mediaMetadataRetriever.getEmbeddedPicture();
        if(artBytes!=null)
        {
            //     InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
            albumImage = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
        }

        return albumImage;
    }

    public void addPlayerListener(){
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    if (player.getPlayWhenReady()) {
                        // In Playing state
                    } else {
                        // In Paused state
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    // In Ended state
                }
            }

            @Override
            public void onPlaybackSuppressionReasonChanged(int playbackSuppressionReason) {

            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
    }

    public void resume() {
        player.setPlayWhenReady(true);
    }

    public void pause() {
        player.setPlayWhenReady(false);
    }

    @Override
    public void onDestroy() {
        if (mediaSession!=null) mediaSession.release();
        if (mediaSessionConnector!=null) mediaSessionConnector.setPlayer(null);
        if (playerNotificationManager!=null) playerNotificationManager.setPlayer(null);
        if (player!=null) {
            player.release();
            player = null;
        }

        if (add_queue_receiver != null) {
            unregisterReceiver(add_queue_receiver);
            add_queue_receiver = null;
        }

        downloadTracker.removeListener(this);

        super.onDestroy();
    }
}
