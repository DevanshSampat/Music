package com.devansh.music;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class CurrentAudioData {
    private static MediaPlayer mediaPlayer;
    private static int position;
    private static ArrayList<AudioModel> audioModelArrayList;
    private static boolean shuffle;
    private static String folder = "All";

    public static String getFolder() {
        return folder;
    }

    public static void setFolder(String folder) {
        CurrentAudioData.folder = folder;
    }

    public static ArrayList<AudioModel> getAudioModelArrayList() {
        return audioModelArrayList;
    }

    public static void setAudioModelArrayList(ArrayList<AudioModel> audioModelArrayList) {
        if(!shuffle) CurrentAudioData.audioModelArrayList = audioModelArrayList;
    }


    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public static void setMediaPlayer(MediaPlayer mediaPlayer) {
        CurrentAudioData.mediaPlayer = mediaPlayer;
    }

    public static int getPosition() {
        return position;
    }

    public static void setPosition(int position) {
        CurrentAudioData.position = position;
    }

    public static boolean isShuffle() {
        return shuffle;
    }

    public static void setShuffle(boolean shuffle) {
        CurrentAudioData.shuffle = shuffle;
        int i,j,tempInt = 0;
        String path = audioModelArrayList.get(position).getPath();
        AudioModel[] audioModels = new AudioModel[audioModelArrayList.size()];
        for (i=0;i<audioModelArrayList.size();i++) audioModels[i] = audioModelArrayList.get(i);
        if(shuffle){
            for(i=0;i<audioModels.length;i++){
                tempInt = tempInt + Calendar.getInstance().get(Calendar.SECOND);
                AudioModel temp = audioModels[i];
                int index = i + tempInt*(tempInt%audioModels.length);
                index = index % audioModels.length;
                if(index<0) index = -1*index;
                audioModels[i] = audioModels[index];
                audioModels[index] = temp;
            }
            for(i=0;i<audioModels.length;i++) {
                if (audioModels[i].getPath().equals(path)) {
                    AudioModel temp = audioModels[i];
                    audioModels[i] = audioModels[0];
                    audioModels[0] = temp;
                    break;
                }
            }
        }
        else{
           for(i=0;i<audioModels.length-1;i++){
               for(j=0;j<audioModels.length-1-i;j++){
                   if(audioModels[j].getName().compareTo(audioModels[j+1].getName())>0){
                       AudioModel temp = audioModels[j];
                       audioModels[j] = audioModels[j+1];
                       audioModels[j+1] = temp;
                   }
               }
           }
        }
        audioModelArrayList.clear();
        for(i=0;i<audioModels.length;i++) audioModelArrayList.add(audioModels[i]);
        for(i=0;i<audioModelArrayList.size();i++){
            if(audioModelArrayList.get(i).getPath().equals(path)){
                position = i;
                break;
            }
        }
    }
}
