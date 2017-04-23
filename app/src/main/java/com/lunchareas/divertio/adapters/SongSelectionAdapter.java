package com.lunchareas.divertio.adapters;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.models.SongData;

import java.util.ArrayList;
import java.util.List;

public class SongSelectionAdapter extends ArrayAdapter<SongData> {

    private static final String TAG = SongSelectionAdapter.class.getName();

    private List<SongData> songDataList;
    private LayoutInflater songListInflater;
    private Context context;
    private SparseBooleanArray selectedSongs;

    public SongSelectionAdapter(Context context, int resourceId, List<SongData> songList) {
        super(context, resourceId, songList);
        this.songDataList = songList;
        this.songListInflater = LayoutInflater.from(context);
        this.context = context;
        this.selectedSongs = new SparseBooleanArray();
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parentView) {
        final RelativeLayout songListLayout = (RelativeLayout) songListInflater.inflate(R.layout.song_selected_layout, parentView, false);

        // Get the parts of a song layout
        ImageView songItemIcon = (ImageView) songListLayout.findViewById(R.id.song_icon);
        TextView songItemName = (TextView) songListLayout.findViewById(R.id.song_name);
        TextView songItemArtist = (TextView) songListLayout.findViewById(R.id.song_composer);

        // Set the parts equal to the corresponding song
        SongData songItem = songDataList.get(position);
        songItemIcon.setImageDrawable(songItem.getSongIcon());
        songItemName.setText(songItem.getSongName());
        songItemArtist.setText(songItem.getSongArtist());

        // Assertions
        //Log.d(TAG, "Song Name: " + songItem.getSongName());
        //Log.d(TAG, "Song Artist: " + songItem.getSongArtist());

        // Set position as tag
        songListLayout.setTag(position);
        return songListLayout;
    }

    @Override
    public void remove(SongData songData) {
        songDataList.remove(songData);
        notifyDataSetChanged();
    }

    public void toggleSelection(int pos) {
        // !contains returns false if it already exists
        selectSong(pos, !selectedSongs.get(pos));
    }

    public void removeSelection() {
        selectedSongs = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectSong(Integer pos, boolean checked) {
        // Add if checked, remove if not checked
        if (checked) {
            selectedSongs.put(pos, checked);
        } else {
            selectedSongs.delete(pos);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        Log.d(TAG, Integer.toString(selectedSongs.size()));
        return selectedSongs.size();
    }

    public SparseBooleanArray getSelectedSongs() {
        return selectedSongs;
    }
}
