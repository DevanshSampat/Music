package com.devansh.music;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private ArrayList<String> folders;
    private Context context;
    public ArrayList<String> getFolders() {
        return folders;
    }

    public void setFolders(ArrayList<String> folders) {
        this.folders = folders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FolderAdapter.ViewHolder holder, int position) {
        ((TextView)holder.itemView.findViewById(R.id.text)).setText(folders.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CurrentAudioData.getFolder().equals(folders.get(position))) return;
                CurrentAudioData.setFolder(folders.get(position));
                CurrentAudioData.setShuffle(false);
                CurrentAudioData.setPosition(0);
                CurrentAudioData.setAudioModelArrayList(null);
                context.sendBroadcast(new Intent("FOLDER_CHANGED"));
                File file = new File(context.getFilesDir(),"Folder.txt");
                if(!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    FileOutputStream fos = context.openFileOutput("Folder.txt",Context.MODE_PRIVATE);
                    fos.write(folders.get(position).getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
