package com.devansh.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayerActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar seekbar;
    private boolean seeking;
    private int songNumber;
    private boolean repeat;
    private PhoneIncomingCallListener phoneIncomingCallListener;
    private BroadcastReceiver playReceiver, pauseReceiver, playPauseReceiver, nextReceiver, previousReceiver;
    private long time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        phoneIncomingCallListener = new PhoneIncomingCallListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try {
            ((TextView) findViewById(R.id.title)).setText(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getName());
            ((TextView) findViewById(R.id.album)).setText(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getAlbum());
        } catch (Exception e) {
            finish();
            NotificationManagerCompat.from(this).cancel(1080);
            return;
        }
        if(CurrentAudioData.isShuffle()){
            ((ImageView)findViewById(R.id.shuffle)).setImageResource(R.drawable.ic_baseline_shuffle_purple_24);
        }
        else{
            ((ImageView)findViewById(R.id.shuffle)).setImageResource(R.drawable.ic_baseline_shuffle_24);
        }
        if(repeat){
            ((ImageView)findViewById(R.id.repeat)).setImageResource(R.drawable.ic_baseline_repeat_one_purple_24);
        }
        else{
            ((ImageView)findViewById(R.id.repeat)).setImageResource(R.drawable.ic_baseline_repeat_24);
        }
        if(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getCover()!=null) ((ImageView)findViewById(R.id.image)).setImageBitmap(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getCover());
        if(CurrentAudioData.getMediaPlayer()==null) {
            mediaPlayer = new MediaPlayer();
            CurrentAudioData.setMediaPlayer(mediaPlayer);
        }
        mediaPlayer = CurrentAudioData.getMediaPlayer();
        if(!getIntent().hasExtra("open_by_notification")) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.start();
                else mediaPlayer.pause();
            } catch (Exception e) {
                e.printStackTrace();
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getPath());
                    mediaPlayer.prepare();
                    CurrentAudioData.setMediaPlayer(mediaPlayer);
                    countTime();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        playReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    mediaPlayer.start();
                    sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
                }catch (Exception e){}
            }
        };
        pauseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    mediaPlayer.pause();
                    sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
                }catch (Exception e){}
            }
        };
        playPauseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(time+1000>System.currentTimeMillis()) return;
                time = System.currentTimeMillis();
                if(mediaPlayer.isPlaying()) mediaPlayer.pause();
                else mediaPlayer.start();
                sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
            }
        };
        nextReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                findViewById(R.id.next).callOnClick();
                sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
            }
        };
        previousReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                findViewById(R.id.previous).callOnClick();
                sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
            }
        };
        BroadcastReceiver turnOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try{
                    mediaPlayer.release();
                    getApplicationContext().unregisterReceiver(playReceiver);
                    getApplicationContext().unregisterReceiver(pauseReceiver);
                    getApplicationContext().unregisterReceiver(playPauseReceiver);
                    getApplicationContext().unregisterReceiver(nextReceiver);
                    getApplicationContext().unregisterReceiver(previousReceiver);
                    ((ImageView)findViewById(R.id.play_pause_image)).setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    ((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).listen(phoneIncomingCallListener, PhoneStateListener.LISTEN_NONE);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        registerReceiver(turnOffReceiver,new IntentFilter("STOP"));
        registerReceiver(playReceiver,new IntentFilter("PLAY"));
        registerReceiver(pauseReceiver,new IntentFilter("PAUSE"));
        registerReceiver(pauseReceiver,new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(playPauseReceiver,new IntentFilter("TOGGLE"));
        registerReceiver(previousReceiver,new IntentFilter("PREVIOUS"));
        registerReceiver(nextReceiver,new IntentFilter("NEXT"));
        ((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).listen(phoneIncomingCallListener, PhoneStateListener.LISTEN_CALL_STATE);
        NotificationChannel channel = new NotificationChannel("general",
                "General", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Music info here");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        songNumber = CurrentAudioData.getPosition();
        ArrayList<AudioModel> audioModelArrayList = CurrentAudioData.getAudioModelArrayList();
        findViewById(R.id.repeat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repeat = !repeat;
                if(repeat){
                    ((ImageView)findViewById(R.id.repeat)).setImageResource(R.drawable.ic_baseline_repeat_one_purple_24);
                }
                else{
                    ((ImageView)findViewById(R.id.repeat)).setImageResource(R.drawable.ic_baseline_repeat_24);
                }
            }
        });
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    songNumber = CurrentAudioData.getPosition();
                    if(songNumber<audioModelArrayList.size()-1){
                        songNumber++;
                        CurrentAudioData.setPosition(songNumber);
                        sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
                        prepareSong();
                    }
                    else Toast.makeText(MusicPlayerActivity.this, "Reached end of queue", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    songNumber = CurrentAudioData.getPosition();
                    if(songNumber>0){
                        songNumber--;
                        CurrentAudioData.setPosition(songNumber);
                        sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
                        prepareSong();
                    }
                    else Toast.makeText(MusicPlayerActivity.this, "Reached start of queue", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mediaPlayer = CurrentAudioData.getMediaPlayer();
        seekbar = findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String str = "";
                int position = seekBar.getProgress();
                position = position / 1000;
                if (position > 3600) str = str + position / 3600 + ":";
                position = position % 3600;
                if (position / 60 < 10) str = str + "0";
                str = str + position / 60 + ":";
                position = position % 60;
                if (position < 10) str = str + "0";
                str = str + position;
                ((TextView) findViewById(R.id.current_time)).setText(str);
                str = "";
                position = seekBar.getMax();
                position = position / 1000;
                if (position > 3600) str = str + position / 3600 + ":";
                position = position % 3600;
                if (position / 60 < 10) str = str + "0";
                str = str + position / 60 + ":";
                position = position % 60;
                if (position < 10) str = str + "0";
                str = str + position;
                ((TextView) findViewById(R.id.total_time)).setText(str);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seeking = false;
                mediaPlayer.seekTo(seekBar.getProgress());
                sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
            }
        });
        findViewById(R.id.play_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        ((ImageView) findViewById(R.id.play_pause_image)).setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    } else {
                        mediaPlayer.start();
                        ((ImageView) findViewById(R.id.play_pause_image)).setImageResource(R.drawable.ic_baseline_pause_24);
                    }
                    sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
                } catch (Exception e) {
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getPath());
                        mediaPlayer.prepare();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    mediaPlayer.start();
                    CurrentAudioData.setMediaPlayer(mediaPlayer);
                    countTime();
                    e.printStackTrace();
                    registerReceiver(playReceiver,new IntentFilter("PLAY"));
                    registerReceiver(pauseReceiver,new IntentFilter("PAUSE"));
                    registerReceiver(pauseReceiver,new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
                    registerReceiver(playPauseReceiver,new IntentFilter("TOGGLE"));
                    registerReceiver(previousReceiver,new IntentFilter("PREVIOUS"));
                    registerReceiver(nextReceiver,new IntentFilter("NEXT"));
                    sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(repeat){
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                    sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
                }
                else findViewById(R.id.next).callOnClick();
            }
        });
        findViewById(R.id.shuffle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!CurrentAudioData.isShuffle()){
                    ((ImageView)findViewById(R.id.shuffle)).setImageResource(R.drawable.ic_baseline_shuffle_purple_24);
                    CurrentAudioData.setShuffle(true);
                    songNumber = CurrentAudioData.getPosition();
                }
                else{
                    ((ImageView)findViewById(R.id.shuffle)).setImageResource(R.drawable.ic_baseline_shuffle_24);
                    CurrentAudioData.setShuffle(false);
                    songNumber = CurrentAudioData.getPosition();
                }
            }
        });
        countTime();
        if(getIntent().hasExtra("open_by_notification")) return;
        try{
            mediaPlayer.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.setDataSource(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getPath());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    registerReceiver(previousReceiver,new IntentFilter("PREVIOUS"));
                    registerReceiver(nextReceiver,new IntentFilter("NEXT"));
                    sendBroadcast(new Intent("PLAYBACK_STATE_CHANGED"));
                }
            });
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException ie){
            CurrentAudioData.setMediaPlayer(null);
            finish();
            startActivity(getIntent());
        }
    }

    private void countTime() {
        try{
            if(!seeking) {
                if(mediaPlayer.isPlaying()) ((ImageView)findViewById(R.id.play_pause_image)).setImageResource(R.drawable.ic_baseline_pause_24);
                else ((ImageView)findViewById(R.id.play_pause_image)).setImageResource(R.drawable.ic_baseline_play_arrow_24);
                String str = "";
                int position = mediaPlayer.getCurrentPosition();
                position = position / 1000;
                if (position > 3600) str = str + position / 3600 + ":";
                position = position % 3600;
                if (position / 60 < 10) str = str + "0";
                str = str + position / 60 + ":";
                position = position % 60;
                if (position < 10) str = str + "0";
                str = str + position;
                ((TextView) findViewById(R.id.current_time)).setText(str);
                str = "";
                position = mediaPlayer.getDuration();
                position = position / 1000;
                if (position > 3600) str = str + position / 3600 + ":";
                position = position % 3600;
                if (position / 60 < 10) str = str + "0";
                str = str + position / 60 + ":";
                position = position % 60;
                if (position < 10) str = str + "0";
                str = str + position;
                ((TextView) findViewById(R.id.total_time)).setText(str);
                seekbar.setMax(mediaPlayer.getDuration());
                seekbar.setProgress(mediaPlayer.getCurrentPosition());
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    countTime();
                }
            },100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void prepareSong(){
        try{
            unregisterReceiver(previousReceiver);
            unregisterReceiver(nextReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((TextView)findViewById(R.id.title)).setText(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getName());
        ((TextView)findViewById(R.id.album)).setText(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getAlbum());
        if(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getCover()!=null) ((ImageView)findViewById(R.id.image)).setImageBitmap(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getCover());
        else ((ImageView)findViewById(R.id.image)).setImageResource(R.drawable.music);
        if(CurrentAudioData.getMediaPlayer()==null) {
            mediaPlayer = new MediaPlayer();
            CurrentAudioData.setMediaPlayer(mediaPlayer);
        }
        mediaPlayer = CurrentAudioData.getMediaPlayer();
        try{
            mediaPlayer.reset();
        } catch (Exception e) {
            mediaPlayer = new MediaPlayer();
            e.printStackTrace();
        }
        try {
            mediaPlayer.setDataSource(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            CurrentAudioData.setMediaPlayer(mediaPlayer);
            countTime();
            ((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).listen(phoneIncomingCallListener, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try{
            if(mediaPlayer.isPlaying()) mediaPlayer.start();
            else mediaPlayer.pause();
        } catch (Exception e) {
            e.printStackTrace();
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(CurrentAudioData.getAudioModelArrayList().get(CurrentAudioData.getPosition()).getPath());
                mediaPlayer.prepare();
                CurrentAudioData.setMediaPlayer(mediaPlayer);
                countTime();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}