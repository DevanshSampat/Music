package com.devansh.music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MusicService extends Service {
    private MediaSessionCompat mediaSession;
    private MediaSessionManager mediaSessionManager;
    private int playbackStateCode;
    private long time;
    private BroadcastReceiver songChangeReceiver;
    private BroadcastReceiver closeReceiver;
    private MediaSessionCompat.Callback callback;
    private long timeForNextAction;
    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                playbackStateCode = PlaybackStateCompat.STATE_PLAYING;
                updateMetadata();
            }
        };
        closeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    mediaSession.release();
                    NotificationManagerCompat.from(getApplicationContext()).cancel(1080);
                    mediaSession = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        registerReceiver(closeReceiver,new IntentFilter("STOP"));
        registerReceiver(songChangeReceiver,new IntentFilter("PLAYBACK_STATE_CHANGED"));
        Intent intentForMain = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intentForMain.putExtra(Settings.EXTRA_APP_PACKAGE,BuildConfig.APPLICATION_ID);
        intentForMain.putExtra(Settings.EXTRA_CHANNEL_ID,"service");
        NotificationCompat.Builder serviceBuilder = new NotificationCompat.Builder(getApplicationContext(), "service")
                .setAutoCancel(true)
                .setColor(Color.BLUE)
                .setContentTitle("Service is running")
                .setContentText("Tap to disable this notification")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, intentForMain, PendingIntent.FLAG_UPDATE_CURRENT));
        super.startForeground(450, serviceBuilder.build());
        mediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(),"Music");
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS|MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        updateMetadata();
        callback = new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                if(CurrentAudioData.getMediaPlayer()==null) return false;
                KeyEvent keyEvent = (KeyEvent) mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if(time+1000<System.currentTimeMillis()){
                    time = System.currentTimeMillis();
                    try {
                        switch (keyEvent.getKeyCode()) {
                            case KeyEvent.KEYCODE_MEDIA_PLAY:
                                sendBroadcast(new Intent("PLAY"));
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                                sendBroadcast(new Intent("PAUSE"));
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                                sendBroadcast(new Intent("TOGGLE"));
                                break;
                            case KeyEvent.KEYCODE_MEDIA_NEXT:
                                sendBroadcast(new Intent("NEXT"));
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                                sendBroadcast(new Intent("PREVIOUS"));
                                break;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
            }

            @Override
            public void onPause() {
                super.onPause();
            }

            @Override
            public void onPlay() {
                super.onPlay();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                CurrentAudioData.getMediaPlayer().seekTo(Math.toIntExact(pos));
                mediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING,
                        pos,
                        1)
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                );
            }

        };
        mediaSession.setCallback(callback);
    }

    private void updateMetadata() {
        try {
            if (CurrentAudioData.getAudioModelArrayList() == null) return;
            if(mediaSession==null)
            {
                mediaSession = new MediaSessionCompat(getApplicationContext(),"Music");
                mediaSession.setActive(true);
                mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS|MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
            }
            mediaSession.setCallback(callback);
            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
            AudioModel audioModel = CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition());
            if (audioModel.getCover() != null)
                builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, audioModel.getCover());
            else
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.drawable.music));
            if (audioModel.getDuration() > 0)
                builder.putLong(MediaMetadata.METADATA_KEY_DURATION, CurrentAudioData.getMediaPlayer().getDuration());
            builder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, audioModel.getName());
            builder.putString(MediaMetadata.METADATA_KEY_ALBUM, audioModel.getAlbum());
            builder.putString(MediaMetadata.METADATA_KEY_ARTIST, audioModel.getArtist());
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, audioModel.getName());
            builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,audioModel.getArtist());
            mediaSession.setMetadata(builder.build());
            MediaPlayer mediaPlayer = CurrentAudioData.getMediaPlayer();
            if (mediaPlayer.isPlaying()) playbackStateCode = PlaybackStateCompat.STATE_PLAYING;
            else playbackStateCode = PlaybackStateCompat.STATE_PAUSED;
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(playbackStateCode,
                    mediaPlayer.getCurrentPosition(),
                    1)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
            );
            Intent musicPlayerIntent = new Intent(this, MusicPlayerActivity.class);
            musicPlayerIntent.putExtra("open_by_notification", true);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "general")
                    .setColor(Color.parseColor("#1e88e5"))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(PendingIntent.getActivity(this, 9, musicPlayerIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setAutoCancel(true)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(1).setMediaSession(mediaSession.getSessionToken()))
                    .setContentTitle(audioModel.getName())
                    .setContentText(audioModel.getArtist()==null?"<unknown>":audioModel.getArtist());
            notificationBuilder.addAction(R.drawable.ic_baseline_skip_previous_24, "previous", PendingIntent.getBroadcast(this, 1, new Intent("PREVIOUS"), 0));
            if (mediaPlayer.isPlaying()) {
                notificationBuilder.addAction(R.drawable.ic_baseline_pause_24, "pause", PendingIntent.getBroadcast(this, 2, new Intent("PAUSE"), 0));
            } else
                notificationBuilder.addAction(R.drawable.ic_baseline_play_arrow_24, "play", PendingIntent.getBroadcast(this, 2, new Intent("PLAY"), 0));
            notificationBuilder.addAction(R.drawable.ic_baseline_skip_next_24, "next", PendingIntent.getBroadcast(this, 3, new Intent("NEXT"), 0));
            notificationBuilder.addAction(R.drawable.ic_baseline_close_24, "close", PendingIntent.getBroadcast(this, 4, new Intent("STOP"), 0));
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1080, notificationBuilder.build());
            try{
                File file = new File(getApplicationContext().getFilesDir(),"LastMusicPath.txt");
                if(!file.exists()) file.createNewFile();
                FileOutputStream fos = openFileOutput("LastMusicPath.txt",MODE_PRIVATE);
                fos.write((audioModel.getPath()+"\n").getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.setActive(false);
        mediaSession.setMetadata(null);
        mediaSession.getController().getTransportControls().stop();
        mediaSession.release();
        NotificationManagerCompat.from(this).cancel(1080);
    }
}