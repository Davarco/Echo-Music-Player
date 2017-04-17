package com.lunchareas.divertio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.models.SongData;

import java.util.List;

public class SongAdapter extends BaseAdapter {

    private static final String TAG = SongAdapter.class.getName();

    private List<SongData> songDataList;
    private LayoutInflater songListInflater;

    public SongAdapter(Context c, List<SongData> songList) {
        this.songDataList = songList;
        this.songListInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songDataList.size();
    }

    // not used
    @Override
    public Object getItem(int arg0) {
        return null;
    }

    // not used
    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {
        RelativeLayout songListLayout = (RelativeLayout) songListInflater.inflate(R.layout.song_layout, parentView, false);

        // get the parts of a song layout
        ImageView songItemIcon = (ImageView) songListLayout.findViewById(R.id.song_icon);
        TextView songItemName = (TextView) songListLayout.findViewById(R.id.song_name);
        TextView songItemArtist = (TextView) songListLayout.findViewById(R.id.song_composer);

        // set the parts equal to the corresponding song
        SongData songItem = songDataList.get(position);
        songItemIcon.setImageDrawable(songItem.getSongIcon());
        songItemName.setText(songItem.getSongName());
        songItemArtist.setText(songItem.getSongArtist());

        // Assertions
        //Log.d(TAG, "Song Name: " + songItem.getSongName());
        //Log.d(TAG, "Song Artist: " + songItem.getSongArtist());

        // set position as tag
        songListLayout.setTag(position);
        return songListLayout;
    }
}
