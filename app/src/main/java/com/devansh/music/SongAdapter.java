package com.devansh.music;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private ArrayList<AudioModel> audioModelArrayList = new ArrayList<>();
    private Context context;

    public SongAdapter(ArrayList<AudioModel> audioModelArrayList) {
        this.audioModelArrayList = audioModelArrayList;
    }

    public void setAudioModelArrayList(ArrayList<AudioModel> audioModelArrayList) {
        this.audioModelArrayList = audioModelArrayList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        AudioModel audioModel = audioModelArrayList.get(position);
        if(audioModel.getCover()!=null)
            ((ImageView)holder.itemView.findViewById(R.id.image)).setImageBitmap(audioModel.getCover());
        else ((ImageView)holder.itemView.findViewById(R.id.image)).setImageResource(R.drawable.music);
        ((TextView)holder.itemView.findViewById(R.id.name)).setText(audioModel.getName());
        ((TextView)holder.itemView.findViewById(R.id.artists)).setText(audioModel.getAlbum());
        long duration = audioModel.getDuration();
        duration = duration/1000;
        int min = (int)duration/60;
        int sec = (int)duration%60;
        String str = "";
        if(min<10) str = str + "0";
        str = str + min + ":";
        if(sec<10) str = str + "0";
        str = str + sec;
        ((TextView)holder.itemView.findViewById(R.id.duration)).setText(str);
        holder.itemView.findViewById(R.id.layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,MusicPlayerActivity.class);
                CurrentAudioData.setPosition(position);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        try{
            return audioModelArrayList.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
