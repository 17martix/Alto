package com.structurecode.alto.Services;

import android.app.Notification;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationHelper;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;
import com.structurecode.alto.Download.SongDownloadManager;
import com.structurecode.alto.R;

import java.util.List;

import static com.structurecode.alto.Services.PlayerService.DOWNLOAD_COMPLETED;

public class SongDownloadService extends DownloadService {

    private static final String CHANNEL_ID = "com.structurecode.alto.services.download.chanel.id";
    private static final int JOB_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    private static int nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1;
    private DownloadNotificationHelper notificationHelper;

    public SongDownloadService() {
        super(FOREGROUND_NOTIFICATION_ID, DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL, CHANNEL_ID,
                R.string.exo_download_notification_channel_name,/* channelDescriptionResourceId= */ 0);
        nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHelper = new DownloadNotificationHelper(this, CHANNEL_ID);
    }

    @Override
    protected DownloadManager getDownloadManager() {
        return SongDownloadManager.getDownloadManager(SongDownloadService.this);
    }

    @Override
    protected PlatformScheduler getScheduler() {
        return Util.SDK_INT >= 21 ? new PlatformScheduler(this, JOB_ID) : null;
    }

    @Override
    protected Notification getForegroundNotification(List<Download> downloads) {
        return notificationHelper.buildProgressNotification(
                R.drawable.download, /* contentIntent= */ null, /* message= */ null, downloads);
    }

    @Override
    protected void onDownloadChanged(Download download) {
        Intent intent1 = new Intent();
        intent1.setAction(DOWNLOAD_COMPLETED);
        sendBroadcast(intent1);
        /*Notification notification;
        if (download.state == Download.STATE_COMPLETED) {
            notification =
                    notificationHelper.buildDownloadCompletedNotification(
                            R.drawable.done,
                            *//* contentIntent= *//* null,
                            Util.fromUtf8Bytes(download.request.data));
        } else if (download.state == Download.STATE_FAILED) {
            notification =
                    notificationHelper.buildDownloadFailedNotification(
                            R.drawable.failed,
                            *//* contentIntent= *//* null,
                            Util.fromUtf8Bytes(download.request.data));
        } else {
            return;
        }
        NotificationUtil.setNotification(this, nextNotificationId++, notification);*/
    }
}
