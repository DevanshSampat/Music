package com.devansh.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<AudioModel> audioModels;
    private RecyclerView recyclerView;
    private boolean isLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        NotificationChannel channel = new NotificationChannel("service",
                "Service Notifications", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Turn off these notifications, it won't impact the app");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        startForegroundService(new Intent(this,MusicService.class));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getAllAudioFiles();
            }
        },300);
    }

    private void getAllAudioFiles() {
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},99);
            return;
        }
        finish();
        startActivity(new Intent(this,LoadActivity.class));
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            projection = new String[]{
                    MediaStore.Audio.AudioColumns.DATA,
                    MediaStore.Audio.AudioColumns.ALBUM_ID,
                    MediaStore.Audio.AudioColumns.TITLE,
                    MediaStore.Audio.AudioColumns.TRACK,
                    MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                    MediaStore.Audio.AudioColumns.YEAR,
                    MediaStore.Audio.AudioColumns.ALBUM,
                    MediaStore.Audio.AudioColumns.ARTIST,
                    MediaStore.Audio.AudioColumns.DURATION
            };
            audioModels = new ArrayList<>();
            isLoading = true;
            Cursor cursor = getContentResolver().query(uri,projection,null,null);
            if(cursor!=null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(cursor.moveToNext()){
                            AudioModel audioModel = new AudioModel();
                            String path = cursor.getString(0);
                            String album_id = cursor.getString(1);
                            String name = cursor.getString(2);
                            String track = cursor.getString(3);
                            String display_name = cursor.getString(4);
                            String year = cursor.getString(5);
                            String album = cursor.getString(6);
                            String artist = cursor.getString(7);
                            String duration = cursor.getString(8);
                            audioModel.setAlbum(album);
                            audioModel.setPath(path);
                            audioModel.setName(name);
                            audioModel.setArtist(artist);
                            try{
                                audioModel.setDuration(Long.parseLong(duration));
                            }
                            catch (Exception e){}
                            Uri coverUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),Long.parseLong(album_id));
                            try {
                                audioModel.setCover(BitmapFactory.decodeStream(getContentResolver().openInputStream(coverUri)));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            audioModels.add(audioModel);
                            Log.println(Log.ASSERT,"duration",duration+" ms");
                        }
                        AudioModel[] audioModelArray = new AudioModel[audioModels.size()];
                        int i,j;
                        for(i=0;i<audioModels.size();i++) audioModelArray[i] = audioModels.get(i);
                        for(i=0;i<audioModelArray.length-1;i++){
                            for(j=0;j<audioModelArray.length-1-i;j++){
                                if(audioModelArray[j].getName().toLowerCase().compareTo(audioModelArray[j+1].getName().toLowerCase())>0){
                                    AudioModel temp = audioModelArray[j];
                                    audioModelArray[j] = audioModelArray[j+1];
                                    audioModelArray[j+1] = temp;
                                }
                            }
                        }
                        audioModels.clear();
                        for(i=0;i<audioModelArray.length;i++) audioModels.add(audioModelArray[i]);
                        CurrentAudioData.setAudioModelArrayList(audioModels);
                        sendBroadcast(new Intent("MUSIC_LIST_PREPARED"));
                        try {
                            FileInputStream fis = openFileInput("LastMusicPath.txt");
                            String str = new BufferedReader(new InputStreamReader(fis)).readLine();
                            if(str==null) return;
                            for (i = 0; i < audioModelArray.length; i++) {
                                if(str.equals(audioModelArray[i].getPath())){
                                    if(!CurrentAudioData.isShuffle()) CurrentAudioData.setPosition(i);
                                    sendBroadcast(new Intent("MUSIC_LIST_PREPARED"));
                                    break;
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        }
        else{
            projection = new String[]{
                    MediaStore.Audio.AudioColumns.DATA,
                    MediaStore.Audio.AudioColumns.ALBUM_ID,
                    MediaStore.Audio.AudioColumns.TITLE,
                    MediaStore.Audio.AudioColumns.TRACK,
                    MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                    MediaStore.Audio.AudioColumns.YEAR,
            };
            audioModels = new ArrayList<>();
            isLoading = true;
            Cursor cursor = getContentResolver().query(uri,projection,null,null);
            if(cursor!=null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(cursor.moveToNext()){
                            AudioModel audioModel = new AudioModel();
                            String path = cursor.getString(0);
                            String album_id = cursor.getString(1);
                            String name = cursor.getString(2);
                            String track = cursor.getString(3);
                            String display_name = cursor.getString(4);
                            String year = cursor.getString(5);
                            /*String album = cursor.getString(6);
                            String artist = cursor.getString(7);
                            String duration = cursor.getString(8);
                            */
                            String artist="",album="",duration="";
                            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                            Uri uriMusicFile = FileProvider.getUriForFile(getApplicationContext(),BuildConfig.APPLICATION_ID+".provider",
                                    new File(path));
                            mediaMetadataRetriever.setDataSource(MainActivity.this,uriMusicFile);
                            artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                            album = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                            duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            audioModel.setAlbum(album);
                            audioModel.setPath(path);
                            audioModel.setName(name);
                            audioModel.setArtist(artist);
                            try{
                                audioModel.setDuration(Long.parseLong(duration));
                            }
                            catch (Exception e){}
                            Uri coverUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),Long.parseLong(album_id));
                            try {
                                audioModel.setCover(BitmapFactory.decodeStream(getContentResolver().openInputStream(coverUri)));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            audioModels.add(audioModel);
                            Log.println(Log.ASSERT,"duration",duration+" ms");
                        }
                        AudioModel[] audioModelArray = new AudioModel[audioModels.size()];
                        int i,j;
                        for(i=0;i<audioModels.size();i++) audioModelArray[i] = audioModels.get(i);
                        for(i=0;i<audioModelArray.length-1;i++){
                            for(j=0;j<audioModelArray.length-1-i;j++){
                                if(audioModelArray[j].getName().toLowerCase().compareTo(audioModelArray[j+1].getName().toLowerCase())>0){
                                    AudioModel temp = audioModelArray[j];
                                    audioModelArray[j] = audioModelArray[j+1];
                                    audioModelArray[j+1] = temp;
                                }
                            }
                        }
                        audioModels.clear();
                        for(i=0;i<audioModelArray.length;i++) audioModels.add(audioModelArray[i]);
                        CurrentAudioData.setAudioModelArrayList(audioModels);
                        sendBroadcast(new Intent("MUSIC_LIST_PREPARED"));
                        try {
                            FileInputStream fis = openFileInput("LastMusicPath.txt");
                            String str = new BufferedReader(new InputStreamReader(fis)).readLine();
                            if(str==null) return;
                            for (i = 0; i < audioModelArray.length; i++) {
                                if(str.equals(audioModelArray[i].getPath())){
                                    if(!CurrentAudioData.isShuffle()) CurrentAudioData.setPosition(i);
                                    sendBroadcast(new Intent("MUSIC_LIST_PREPARED"));
                                    break;
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==99&&grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
            getAllAudioFiles();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}