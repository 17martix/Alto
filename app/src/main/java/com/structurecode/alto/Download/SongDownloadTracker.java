package com.structurecode.alto.Download;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadCursor;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadIndex;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;
import com.structurecode.alto.R;
import com.structurecode.alto.Services.SongDownloadService;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/** Tracks media that has been downloaded. */
public class SongDownloadTracker {
    /** Listens for changes in the tracked downloads. */
    public interface Listener {

        /** Called when the tracked downloads changed. */
        void onDownloadsChanged();
    }

    private static final String TAG = "DownloadTracker";

    private final Context context;
    private final DataSource.Factory dataSourceFactory;
    private final CopyOnWriteArraySet<Listener> listeners;
    private final HashMap<Uri, Download> downloads;
    private final DownloadIndex downloadIndex;
    private final DefaultTrackSelector.Parameters trackSelectorParameters;

    @Nullable
    private StartDownloadDialogHelper startDownloadDialogHelper;

    public SongDownloadTracker(
            Context context, DataSource.Factory dataSourceFactory, DownloadManager downloadManager) {
        this.context = context.getApplicationContext();
        this.dataSourceFactory = dataSourceFactory;
        listeners = new CopyOnWriteArraySet<>();
        downloads = new HashMap<>();
        downloadIndex = downloadManager.getDownloadIndex();
        trackSelectorParameters = DownloadHelper.getDefaultTrackSelectorParameters(context);
        downloadManager.addListener(new DownloadManagerListener());
        loadDownloads();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isDownloaded(Uri uri) {
        Download download = downloads.get(uri);
        return download != null && download.state != Download.STATE_FAILED;
    }

    public DownloadRequest getDownloadRequest(Uri uri) {
        Download download = downloads.get(uri);
        return download != null && download.state != Download.STATE_FAILED ? download.request : null;
    }

    public void toggleDownload(String name, Uri uri) {
        Download download = downloads.get(uri);
        if (download != null) {
            DownloadService.sendRemoveDownload(context, SongDownloadService.class, download.request.id, false);

        } else {
            if (startDownloadDialogHelper != null) {
                startDownloadDialogHelper.release();
            }
            startDownloadDialogHelper =
                    new StartDownloadDialogHelper(getDownloadHelper(uri), name);
        }
    }

    private void loadDownloads() {
        try (DownloadCursor loadedDownloads = downloadIndex.getDownloads()) {
            while (loadedDownloads.moveToNext()) {
                Download download = loadedDownloads.getDownload();
                downloads.put(download.request.uri, download);
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to query downloads", e);
        }
    }

    private DownloadHelper getDownloadHelper(Uri uri) {
        return DownloadHelper.forProgressive(context, uri);
    }

    private class DownloadManagerListener implements DownloadManager.Listener {

        @Override
        public void onDownloadChanged(DownloadManager downloadManager, Download download) {
            downloads.put(download.request.uri, download);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }

        @Override
        public void onDownloadRemoved(DownloadManager downloadManager, Download download) {
            downloads.remove(download.request.uri);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }
    }

    private final class StartDownloadDialogHelper implements DownloadHelper.Callback {

        private final DownloadHelper downloadHelper;
        private final String name;

        public StartDownloadDialogHelper(DownloadHelper downloadHelper, String name) {
            this.downloadHelper = downloadHelper;
            this.name = name;
            downloadHelper.prepare(this);

            DownloadRequest downloadRequest = buildDownloadRequest();
            startDownload(downloadRequest);
        }

        public void release() {
            downloadHelper.release();
        }

        // DownloadHelper.Callback implementation.

        @Override
        public void onPrepared(DownloadHelper helper) {
            if (helper.getPeriodCount() == 0) {
                Log.d(TAG, "No periods found. Downloading entire stream.");
                startDownload();
                downloadHelper.release();
                return;
            }
        }

        @Override
        public void onPrepareError(DownloadHelper helper, IOException e) {
            Toast.makeText(context, R.string.download_start_error, Toast.LENGTH_LONG).show();
            Log.e(TAG, e instanceof DownloadHelper.LiveContentUnsupportedException ? "Downloading live content unsupported"
                    : "Failed to start download", e);
        }

        // Internal methods.
        private void startDownload() {
            startDownload(buildDownloadRequest());
        }
        private void startDownload(DownloadRequest downloadRequest) {
            DownloadService.sendAddDownload(context, SongDownloadService.class, downloadRequest, false);
        }

        private DownloadRequest buildDownloadRequest() {
            return downloadHelper.getDownloadRequest(Util.getUtf8Bytes(name));
        }

    }

}
