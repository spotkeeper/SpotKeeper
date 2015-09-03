package com.anrapps.spotkeeper;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.anrapps.spotkeeper.entity.Track;
import com.anrapps.spotkeeper.util.FileUtils;
import com.anrapps.spotkeeper.util.PrefUtils;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServiceRecord extends Service implements PlayerNotificationCallback {

    private static final String ACTION_PAUSE = "action_pause";
    private static final String ACTION_RESUME = "action_resume";
    private static final String ACTION_CANCEL = "action_cancel";

    private static final String BUNDLE_KEY_EXTRA_TRACK_LIST = "bundle_key_extra_track_list";
    private static final int NOTIFICATION_ID = 2001;

    private static final int STATUS_LOADING_TRACKS = 0;
    private static final int STATUS_RECORDING_TRACKS = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_FINISHED = 3;

    private List<Track> mTrackList;

    private Player mPlayer;
    private MediaRecorder mRecorder = null;

    private int mTotalTrackCount = 0;
    private int mCurrentTrack = 0;
    private int mCurrentState = STATUS_LOADING_TRACKS;

    private static ServiceRecord mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        mTrackList = new ArrayList<>();

        Config playerConfig = new Config(this, Application.getToken(), getString(R.string.spotify_client_id));
        mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override public void onInitialized(Player player) {
                mPlayer.addPlayerNotificationCallback(ServiceRecord.this);
            }
            @Override public void onError(Throwable throwable) {
                Log.e("ActivityMain", "Could not initialize player: " + throwable.getMessage());
            }
        });
        mRecorder = new MediaRecorder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        //TODO: Playback skips automatically to the most recent added tracks
        if (paramIntent != null) {
            final String action = paramIntent.getAction();
            if (action != null && action.equals(ACTION_PAUSE)) {
                mPlayer.seekToPosition(1);
                mPlayer.pause();
                startForeground(STATUS_PAUSED);
                //record pause and file deletes are handled in onPlaybackEvent()
                return START_STICKY;
            } else if (action != null && action.equals(ACTION_RESUME)) {
                mPlayer.resume();
                startForeground(STATUS_RECORDING_TRACKS);
                return START_STICKY;
            } else if (action != null && action.equals(ACTION_CANCEL)) {
                mPlayer.pause();
                stopAndDeleteTrackFile();
                stopSelf();
                return START_STICKY;
            }

            startForeground(mTrackList.size() == 0 ? STATUS_LOADING_TRACKS : STATUS_RECORDING_TRACKS);
            List<Track> tracks = paramIntent.getParcelableArrayListExtra(BUNDLE_KEY_EXTRA_TRACK_LIST);
            mTrackList.addAll(tracks);
            mTotalTrackCount += tracks.size();
            List<String> playUris = new ArrayList<>();
            for (Track track : tracks) {
                playUris.add(track.uri);
            }
            mCurrentState = STATUS_RECORDING_TRACKS;
            mPlayer.play(playUris);
            startForeground(STATUS_RECORDING_TRACKS);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            try {
                mRecorder.reset();
                mRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        mRecorder = null;
        Spotify.destroyPlayer(this);
        mTrackList = null;
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        //This method will be full of messy workarounds until Spotify fixes playback given events
        if (BuildConfig.DEBUG)
            Log.d(getClass().getName(), "Playback event received: " + eventType.name());
        switch (eventType) {
            case TRACK_CHANGED:
                if (mCurrentState == STATUS_FINISHED) return;
                mCurrentTrack = getCurrentPlayingTrack(playerState.trackUri);
                initializePlayerAndRecord();
                startForeground(STATUS_RECORDING_TRACKS);
                mCurrentState = STATUS_RECORDING_TRACKS;
                break;
            case PLAY:
                if (mCurrentState != STATUS_PAUSED) return;
                mCurrentState = STATUS_RECORDING_TRACKS;
                initializePlayerAndRecord();
                break;
            case PAUSE:
                if (mCurrentState == STATUS_FINISHED) return;
                mCurrentState = STATUS_PAUSED;
                stopAndDeleteTrackFile();
                startForeground(STATUS_PAUSED);
                break;
            case LOST_PERMISSION:
                mCurrentState = STATUS_PAUSED;
                stopAndDeleteTrackFile();
                startForeground(STATUS_PAUSED);
                break;
            case END_OF_CONTEXT:
                mCurrentState = STATUS_FINISHED;
                stopForeground(true);
                notifyTaskDone();
                stopRecord();
                stopSelf();
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
        Log.e(getClass().getName(), "onPlaybackError => ErrorType: " + errorType.name() + " | String: " + s);
        stopSelf();
    }

    public static ServiceRecord getInstance() {
        return mInstance;
    }

    public static void loadTracks(Context from, ArrayList<Track> tracks) {
        if (tracks == null) return;
        Intent intent = new Intent(from, ServiceRecord.class);
        intent.putParcelableArrayListExtra(BUNDLE_KEY_EXTRA_TRACK_LIST, tracks);
        from.startService(intent);
    }

    public List<Track> getTrackList() {
        return mTrackList;
    }

    private void startForeground(int status) {

        final Intent i = new Intent(this, ActivityQueue.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent activityIntent = PendingIntent.getActivity(this, 0, i, 0);

        final Intent actionIntent = new Intent();
        actionIntent.setClass(this, getClass());
        actionIntent.setPackage(getClass().getPackage().getName());

        actionIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, actionIntent, 0);

        actionIntent.setAction(ACTION_RESUME);
        PendingIntent resumePendingIntent = PendingIntent.getService(this, 0, actionIntent, 0);

        actionIntent.setAction(ACTION_CANCEL);
        PendingIntent cancelPendingIntent = PendingIntent.getService(this, 0, actionIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setOnlyAlertOnce(true);
        builder.setSmallIcon(R.drawable.ic_drawer_queue);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentIntent(activityIntent);

        switch (status) {
            case STATUS_LOADING_TRACKS:
                builder.setContentTitle(getString(R.string.notification_title_loading));
                break;
            case STATUS_RECORDING_TRACKS:
                builder.setContentTitle(getString(R.string.notification_title_recording));
                builder.setProgress(mTotalTrackCount, mCurrentTrack, false);
                builder.setContentText(mTrackList.get(mCurrentTrack).artistName + " - " + mTrackList.get(mCurrentTrack).name);
                builder.addAction(R.drawable.ic_notification_pause, getString(R.string.notification_action_pause), pausePendingIntent);
                builder.addAction(R.drawable.ic_notification_cancel, getString(R.string.notification_action_cancel), cancelPendingIntent);
                break;
            case STATUS_PAUSED:
                builder.setContentTitle(getString(R.string.notification_title_paused));
                builder.setSmallIcon(R.drawable.ic_notification_pause);
                builder.setProgress(mTotalTrackCount, mCurrentTrack, false);
                builder.setContentText(mTrackList.get(mCurrentTrack).artistName + " - " + mTrackList.get(mCurrentTrack).name + " (paused)");
                builder.addAction(R.drawable.ic_notification_resume, getString(R.string.notification_action_resume), resumePendingIntent);
                builder.addAction(R.drawable.ic_notification_cancel, getString(R.string.notification_action_cancel), cancelPendingIntent);
                break;
        }

        startForeground(NOTIFICATION_ID, builder.build());
    }

    private int getCurrentPlayingTrack(String playingUri) {
        for (Track t : mTrackList) if (t.uri.equals(playingUri)) return mTrackList.indexOf(t);
        throw new NullPointerException("No track for this uri");
    }

    private void initializePlayerAndRecord() {
        if (FileUtils.trackFileExists(mTrackList.get(mCurrentTrack)) && PrefUtils.getSkipFilesEnabled(this))
            mPlayer.skipToNext();
        else {
            stopRecord();
            FileUtils.deleteFileForTrack(mTrackList.get(mCurrentTrack));
            trySetMaxVolume();
            startRecord(mTrackList.get(mCurrentTrack));
        }
    }

    private void stopAndDeleteTrackFile() {
        stopRecord();
        FileUtils.deleteFileForTrack(mTrackList.get(mCurrentTrack));
    }

    private void startRecord(Track track) {
        try {
            File file = FileUtils.getFileForTrack(track);
            FileUtils.mkDirsAndFiles(file);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.REMOTE_SUBMIX);
            mRecorder.setOutputFormat(PrefUtils.getOutputFormat(this));
            mRecorder.setOutputFile(file.toString());
            mRecorder.setAudioEncoder(PrefUtils.getAudioEncoder(this));
            int setting = PrefUtils.getEncodingBitrate(this);
            if (setting != 0) mRecorder.setAudioEncodingBitRate(setting);
            setting = PrefUtils.getSamplingRate(this);
            if (setting != 0) mRecorder.setAudioSamplingRate(setting);
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_encoder_not_supported, Toast.LENGTH_SHORT).show();
            mPlayer.pause();
            stopSelf();
        }
    }

    private void stopRecord() {
        try {
            mRecorder.stop();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        mRecorder.reset();
    }

    private void trySetMaxVolume() {
        if (PrefUtils.getVolumeMaxEnabled(this)) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            final int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI);
        }
    }

    private void notifyTaskDone() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setOnlyAlertOnce(true);
        builder.setSmallIcon(R.drawable.ic_stat_done);
        builder.setContentTitle(getString(R.string.notification_title_finished));
        builder.setTicker(getString(R.string.notification_title_finished));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
