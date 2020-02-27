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
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.structurecode.alto.Download.SongDownloadManager;
import com.structurecode.alto.Download.SongDownloadTracker;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.MetricSong;
import com.structurecode.alto.Models.SearchConfig;
import com.structurecode.alto.Models.Setting;
import com.structurecode.alto.Models.Song;
import com.structurecode.alto.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.get_date_daily;
import static com.structurecode.alto.Helpers.Utils.mAuth;
import static com.structurecode.alto.Helpers.Utils.user;

public class PlayerService extends Service implements SongDownloadTracker.Listener {
    public static final String ADD_TO_QUEUE="com.structurecode.alto.services.player.add.queue";
    public static final String DOWNLOAD_SONG="com.structurecode.alto.services.player.download.song";
    public static final String DOWNLOAD="com.structurecode.alto.services.player.download";
    public static final String REMOVE="com.structurecode.alto.services.player.download.remove";
    public static final String PLAY_SONG="com.structurecode.alto.services.player.play.song";
    public static final String DOWNLOAD_COMPLETED="com.structurecode.alto.services.download.completed";
    public static final String AUDIO_EXTRA="song_extra";
    public static final String AUDIO_LIST_EXTRA="song_list_extra";

    private static final String PLAYBACK_CHANNEL_ID="com.structurecode.alto.services.player.channel.id.alto";
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

    public SimpleExoPlayer player;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private DataSource.Factory dataSourceFactory;
    private SongDownloadTracker downloadTracker;

    private FFmpegMediaMetadataRetriever mediaMetadataRetriever;
    private Bitmap albumImage=null;
    private Setting setting;
    private SearchConfig searchConfig;
    private Timer timer;
    private boolean timer_on=false;

    private Client client;
    private Index index;
    private BroadcastReceiver download_receiver;
    private BroadcastReceiver remove_receiver;
    private Song playing_song;

    @Override
    public void onCreate() {
        super.onCreate();

        context=this;
        playlist=new ArrayList<>();
        timer=new Timer();

        mAuth=FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        setting = new Setting(1,1,0,2,"free");
        searchConfig = new SearchConfig();

        dataSourceFactory = SongDownloadManager.buildDataSourceFactory(context);
        downloadTracker = SongDownloadManager.getDownloadTracker(context);
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
        player.setRepeatMode(setting.getRepeat_mode());
        if (setting.getShuffle_mode()==0) player.setShuffleModeEnabled(false);
        else player.setShuffleModeEnabled(true);

        initialize_broadcasts();
        notification_manager();
        addPlayerListener();
        deviceCheck();
        setting_check();
        configuration_search();

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

    public void setting_check(){
        mAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        final DocumentReference docRef = db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                .collection(Utils.COLLECTION_SETTINGS).document(user.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("ABC", "Listen failed.", e);
                    setting = new Setting(1,1,0,2,"free");
                    return;
                }

                if (snapshot != null && snapshot.exists()) {

                    setting = snapshot.toObject(Setting.class);
                    player.setRepeatMode(setting.getRepeat_mode());
                    if (setting.getShuffle_mode()==0) player.setShuffleModeEnabled(false);
                    else player.setShuffleModeEnabled(true);
                } else {
                    setting = new Setting(1,1,0,2,"free");
                    Log.d("ABC", "Current data: null");
                }
            }
        });
    }

    public void configuration_search(){
        mAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        final DocumentReference docRef = db.collection(Utils.COLLECTION_CONFIGURATION).document("search");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("ABC", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {

                    searchConfig = snapshot.toObject(SearchConfig.class);
                    client = new Client(searchConfig.getApplication_id(), searchConfig.getSearch_key());
                    index = client.getIndex("alto_main_search");

                } else {
                    Log.d("ABC", "Current data: null");
                }
            }
        });
    }

    public void song_played_record(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long currentTime = player.getCurrentPosition();
                long duration = player.getDuration();
                long benchmark = duration/2;
                Log.e("HELLO", "FINALLY IN  "+currentTime);
                Log.e("HELLO", "FINALLY IN  "+duration);
                Log.e("HELLO", "FINALLY IN  "+benchmark);
                if (currentTime>benchmark) {
                    db= FirebaseFirestore.getInstance();
                    Log.e("HELLO", "FINALLY IN  "+benchmark);
                    Song song=playlist.get(player.getCurrentWindowIndex());
                    //String path = user.getUid()+song.getId();

                    DocumentReference doc = db.collection(Utils.COLLECTION_METRICS).document(song.getId());
                    db.runTransaction(new Transaction.Function<Void>() {
                        @Nullable
                        @Override
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            DocumentSnapshot snapshot = transaction.get(doc);
                            String daily_listen_count = snapshot.getString("daily_play");
                            String monthly_listen_count = snapshot.getString("monthly_play");
                            String yearly_listen_count = snapshot.getString("yearly_play");
                            Map<String, Long> users = new HashMap<>();
                            if (snapshot.exists()){
                                users = (Map<String, Long>) snapshot.getData().get("users");
                            }

                            String daily_play;
                            String monthly_play;
                            String yearly_play;
                            if (daily_listen_count==null || daily_listen_count.isEmpty()) daily_play=get_date_daily()+1;
                            else{
                                String date = daily_listen_count.substring(0,8);
                                String count = daily_listen_count.substring(8);
                                Log.e("AAAA", get_date_daily()+"-"+date);
                                if (date.equals(get_date_daily())){
                                    long c = (Integer.parseInt(count))+1;
                                    daily_play = get_date_daily()+c;
                                }else {
                                    daily_play = get_date_daily()+1;
                                }
                            }

                            if (monthly_listen_count==null || monthly_listen_count.isEmpty()) monthly_play=Utils.get_date_monthly()+1;
                            else{
                                String date = monthly_listen_count.substring(0,6);
                                String count = monthly_listen_count.substring(6);
                                if (date.equals(Utils.get_date_monthly())){
                                    long c = (Integer.parseInt(count))+1;
                                    monthly_play = Utils.get_date_monthly()+c;
                                }else {
                                    monthly_play = Utils.get_date_monthly()+1;
                                }
                            }

                            if (yearly_listen_count==null || yearly_listen_count.isEmpty()) yearly_play=Utils.get_date_yearly()+1;
                            else{
                                String date = yearly_listen_count.substring(0,4);
                                String count = yearly_listen_count.substring(4);
                                if (date.equals(Utils.get_date_yearly())){
                                    long c = (Integer.parseInt(count))+1;
                                    yearly_play = Utils.get_date_yearly()+c;
                                }else {
                                    yearly_play = Utils.get_date_yearly()+1;
                                }
                            }

                            if (users==null || users.isEmpty()){
                                users = new HashMap<>();
                                long c = 1;
                                users.put(user.getUid(),c);
                            }else {
                                if (users.containsKey(user.getUid())) {
                                    long c = users.get(user.getUid());
                                    long new_count = c + 1;

                                    if (new_count > 5) {
                                        ArrayList<String> user_list = new ArrayList<>();
                                        for (Map.Entry<String, Long> entry : users.entrySet()) {
                                            if (entry.getValue() > new_count) {
                                                user_list.add(entry.getKey());
                                            }
                                        }

                                        if (user_list.size() > 0) {
                                            String chosen_user = user_list.get(new Random().nextInt(user_list.size()));
                                            Log.e("HELLO1", "RECOM1");
                                            db.collection(Utils.COLLECTION_METRICS).whereGreaterThan("users." + chosen_user, new_count).get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            Log.e("HELLO1", "RECOM2");
                                                            if (queryDocumentSnapshots.size() > 1) {
                                                                DocumentSnapshot snap = null;
                                                                boolean loop = true;
                                                                while (loop) {
                                                                    snap = queryDocumentSnapshots.getDocuments().get(new Random().nextInt(queryDocumentSnapshots.size()));
                                                                    if (snap.getId() != song.getId()) {
                                                                        loop = false;
                                                                    }
                                                                }

                                                                MetricSong recom = snap.toObject(MetricSong.class);
                                                                /*Map<String, Object> recom_song = new HashMap<>();
                                                                recom_song.put("id", snap.getId());
                                                                recom_song.put("artist", snap.getString("artist"));
                                                                recom_song.put("album", snap.getString("album"));
                                                                recom_song.put("path", snap.getString("path"));
                                                                recom_song.put("title", snap.getString("title"));
                                                                recom_song.put("url", snap.getString("url"));
                                                                recom_song.put("daily_play", snap.getString("daily_play"));*/
                                                                db.collection(Utils.COLLECTION_USERS).document(user.getUid()).collection(Utils.COLLECTION_RECOMMENDED).document(snap.getId())
                                                                        .set(recom).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.e("SUCCESS", "New Recommended Song Added");
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("ERROR","FAILLURE  " +e.getMessage());
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("NOOOO","FAILLURE  " +e.getMessage());
                                                }
                                            });
                                        }
                                    }
                                    users.put(user.getUid(), new_count);

                                } else {
                                    long c = 1;
                                    users.put(user.getUid(), c);
                                }
                            }

                            /*Map<String, Object> map = new HashMap<>();
                            map.put("users", users);
                            map.put("id", song.getId());
                            map.put("artist", song.getArtist());
                            map.put("album", song.getAlbum());
                            map.put("path", song.getPath());
                            map.put("title", song.getTitle());
                            map.put("url", song.getUrl());
                            map.put("lyrics", song.getLyrics());
                            map.put("daily_play", daily_play);
                            map.put("monthly_play", monthly_play);
                            map.put("yearly_play", yearly_play);*/
                            MetricSong metricSong = new MetricSong(song,users,daily_play,monthly_play,yearly_play);
                            transaction.set(doc,metricSong);
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e("OOOOOOF","SUCESSSSSS");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("WHHAAAT","FAILLURE  " +e.getMessage());
                        }
                    });


                    /*DocumentReference doc = db.collection(Utils.COLLECTION_METRICS).document(path);
                    db.runTransaction(new Transaction.Function<Void>() {
                        @Nullable
                        @Override
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            DocumentSnapshot snapshot = transaction.get(doc);
                            String listen_count = snapshot.getString("play");
                            int play;
                            if (listen_count==null || listen_count.isEmpty()) play=1;
                            else play=1+Integer.parseInt(listen_count);
                            Map<String, Object> map = new HashMap<>();
                            map.put("user_id", user.getUid());
                            map.put("song_id", song.getId());
                            map.put("song_artist", song.getArtist());
                            map.put("song_album", song.getAlbum());
                            map.put("song_playlist_id", song.getPlaylist_id());
                            map.put("song_path", song.getPath());
                            map.put("song_title", song.getTitle());
                            map.put("song_url", song.getUrl());
                            map.put("date", System.currentTimeMillis());
                            map.put("play", String.valueOf(play));
                            transaction.set(doc,map);
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e("OOOOOOF","SUCESSSSSS");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("WHHAAAT","FAILLURE  " +e.getMessage());
                        }
                    });*/
                    timer_on=false;
                    cancel();
                }
            }
        },0,10000);
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

                String device_id = snapshot.getString("device_id");

                if (device_id==null || device_id.isEmpty()) device_id="empty";

                if (!device_id.equals(Utils.get_device_id(context))) {
                        /*if (player!=null) {
                            player.stop(true);
                            player.release();
                        }*/
                    Intent intent = new Intent();
                    intent.setAction(Utils.DEVICE_CHECK);
                    sendBroadcast(intent);
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

        concatenatingMediaSource=new ConcatenatingMediaSource();
        MediaSource mediaSource = getMedia(song);
        concatenatingMediaSource.addMediaSource(mediaSource);
        playlist.add(song);

        for (int i=0; i<songList.size(); i++){
            if (!songList.get(i).getId().equals(song.getId())){
                mediaSource = getMedia(songList.get(i));
                concatenatingMediaSource.addMediaSource(mediaSource);
                playlist.add(songList.get(i));
            }
        }

        player.prepare(concatenatingMediaSource);
        player.seekTo(0,C.TIME_UNSET);
        player.setPlayWhenReady(true);

        /*for (int i=0; i<mediaSources.length;i++){
            MediaSource mediaSource = getMedia(songList.get(i));
            mediaSources[i]=mediaSource;

            playlist.add(songList.get(i));
        }
        concatenatingMediaSource=new ConcatenatingMediaSource(mediaSources);

        int index=songList.indexOf(song);
        player.prepare(concatenatingMediaSource);
        player.seekTo(index,C.TIME_UNSET);
        player.setPlayWhenReady(true);*/

    }

    public MediaSource getMedia(Song song){
        DownloadRequest downloadRequest = SongDownloadManager.getDownloadTracker(context).getDownloadRequest(Uri.parse(song.getUrl()));
        if (downloadRequest != null) {
            return DownloadHelper.createMediaSource(downloadRequest, dataSourceFactory);
        }

        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(song.getUrl()));
    }

    public void notification_manager(){
        playerNotificationManager=PlayerNotificationManager.createWithNotificationChannel(context, PLAYBACK_CHANNEL_ID, R.string.player_channel_name,
                R.string.player_channel_description, PLAYBACK_NOTIFICATION_ID, new PlayerNotificationManager.MediaDescriptionAdapter() {
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
                        //return null;
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

    /*public void add_record(Song song) {
        Map<String, Object> map = new HashMap<>();
        map.put("user", user.getUid());
        map.put("song", song.getId());

        Log.e("LAUNCHED", "ADD_RECORD");
        DocumentReference doc = db.collection(Utils.COLLECTION_METRICS).document(user.getUid()+song.getId());

        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(doc);
                int listen_count = Integer.parseInt(snapshot.getString("listen_count"))+1;
                transaction.update(doc, "listen_count", listen_count);
                transaction.update(doc, "user", user.getUid());
                transaction.update(doc, "song", song.getId());
                return null;
            }
        });
    }*/

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
                downloadTracker.toggleDownload(song.getTitle(),Uri.parse(song.getUrl()));

            }
        };

        IntentFilter download_filter = new IntentFilter();
        download_filter.addAction(DOWNLOAD);
        download_receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song song=(Song)intent.getParcelableExtra(AUDIO_EXTRA);
                downloadTracker.download_song(song.getTitle(),Uri.parse(song.getUrl()));

            }
        };

        IntentFilter remove_filter = new IntentFilter();
        remove_filter.addAction(REMOVE);
        remove_receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song song=(Song)intent.getParcelableExtra(AUDIO_EXTRA);
                downloadTracker.remove_song(Uri.parse(song.getUrl()));

            }
        };

        IntentFilter play_song_filter = new IntentFilter();
        play_song_filter.addAction(PLAY_SONG);
        play_song_receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song song=(Song)intent.getParcelableExtra(AUDIO_EXTRA);
                Log.e("SONG", song.getTitle());
                ArrayList<Song> list = intent.getParcelableArrayListExtra(AUDIO_LIST_EXTRA);
                play_song(song,list);

            }
        };

        registerReceiver(play_song_receiver,play_song_filter);
        registerReceiver(download_song_receiver,download_song_filter);
        registerReceiver(download_receiver,download_filter);
        registerReceiver(remove_receiver,remove_filter);
        registerReceiver(add_queue_receiver,add_queue_filter);
    }

    public void add_to_queue(Song song){
        MediaSource progressiveMediaSource = getMedia(song);
        playlist.add(song);
        concatenatingMediaSource.addMediaSource(progressiveMediaSource);
    }

    public Bitmap getAlbumImage(Song song){
        Log.e("AAA",song.getUrl());
        mediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(song.getUrl(), new HashMap<String, String>());
            //mediaMetadataRetriever.setDataSource(song.getPath(),new HashMap<String, String>());
            //String albumName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            byte[] artBytes = mediaMetadataRetriever.getEmbeddedPicture();
            if (artBytes != null) {
                //     InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                albumImage = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
            }

            mediaMetadataRetriever.release();
        }catch (Exception e){
            Log.e("error","some errors");
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
            }

            @Override
            public void onPlaybackSuppressionReasonChanged(int playbackSuppressionReason) {
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Map<String, Object> map = new HashMap<>();
                map.put("repeat_mode", repeatMode);

                db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                        .collection(Utils.COLLECTION_SETTINGS).document(user.getUid())
                        .update(map);
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                int value;
                if (shuffleModeEnabled) value=1;
                else value = 0;
                Map<String, Object> map = new HashMap<>();
                map.put("shuffle_mode", value);

                db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                        .collection(Utils.COLLECTION_SETTINGS).document(user.getUid())
                        .update(map);
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

                if (playing_song==null){
                    playing_song = playlist.get(player.getCurrentWindowIndex());
                    if (!timer_on){
                        timer_on=true;
                        song_played_record();
                    }
                }else {
                    if (playlist.get(player.getCurrentWindowIndex()).getId().equals(playing_song.getId())){
                        long b = player.getDuration()/2;
                        if (player.getCurrentPosition() < b){
                            if (!timer_on){
                                timer_on=true;
                                song_played_record();
                            }
                        }
                    }else {
                        playing_song = playlist.get(player.getCurrentWindowIndex());
                        if (!timer_on){
                            timer_on=true;
                            song_played_record();
                        }
                    }
                }
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

        if (play_song_receiver != null) {
            unregisterReceiver(play_song_receiver);
            play_song_receiver = null;
        }

        if (download_song_receiver != null) {
            unregisterReceiver(download_song_receiver);
            download_song_receiver = null;
        }

        downloadTracker.removeListener(this);

        super.onDestroy();
    }

    public SearchConfig getSearchConfig() {
        return searchConfig;
    }

    public Index getIndex() {
        return index;
    }
}
