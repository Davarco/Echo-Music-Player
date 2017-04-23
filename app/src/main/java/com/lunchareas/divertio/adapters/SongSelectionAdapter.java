package com.lunchareas.divertio.adapters;


import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.lunchareas.divertio.models.SongData;

import java.util.List;

public class SongSelectionAdapter extends ArrayAdapter<SongData> {

    private Context context;
    private LayoutInflater layoutInflater;
    private List<SongData> songList;

    public SongSelectionAdapter(Context context, int resourceId, List<SongData> songList) {
        super(context, resourceId, songList);
        this.context = context;
        this.songList = songList;
        this.layoutInflater = LayoutInflater.from(context);
    }
}
