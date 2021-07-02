package com.devansh.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class LoadActivity extends AppCompatActivity {

    private ArrayList<AudioModel> audioModels;
    private RecyclerView recyclerView;
    private BroadcastReceiver loadReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        loadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                        findViewById(R.id.progress_circular).setVisibility(View.GONE);
                        recyclerView.setAdapter(new SongAdapter(CurrentAudioData.getAudioModelArrayList()));
                    recyclerView.scrollToPosition(CurrentAudioData.getPosition());
                    }
                };
        registerReceiver(loadReceiver,new IntentFilter("MUSIC_LIST_PREPARED"));
        if(CurrentAudioData.getAudioModelArrayList()!=null) sendBroadcast(new Intent("MUSIC_LIST_PREPARED"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(loadReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        sendBroadcast(new Intent("MUSIC_LIST_PREPARED"));
    }
}