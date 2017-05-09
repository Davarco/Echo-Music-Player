package com.lunchareas.divertio.adapters;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.models.SongData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SongSelectionAdapter extends ArrayAdapter<SongData> {

    private static final String TAG = SongSelectionAdapter.class.getName();

    private List<SongData> songDataList;
    private LayoutInflater songListInflater;
    private Context activity;
    private List<Integer> selectedSongs;

    public SongSelectionAdapter(Activity activity, int resourceId, List<SongData> songList) {
        super(activity, resourceId, songList);
        this.songDataList = songList;
        this.songListInflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.selectedSongs = new ArrayList<>();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parentView) {

        // Base color on selection
        boolean selected = selectedSongs.contains(position);
        RelativeLayout songLayout;
        if (selected) {
            songLayout = (RelativeLayout) songListInflater.inflate(R.layout.song_selected_layout, parentView, false);
        } else {
            songLayout = (RelativeLayout) songListInflater.inflate(R.layout.song_layout, parentView, false);
        }
        final RelativeLayout songListLayout = songLayout;

        // Get the parts of a song layout
        ImageView songItemIcon = (ImageView) songListLayout.findViewById(R.id.song_icon);
        ImageView songOverflowIcon = (ImageView) songListLayout.findViewById(R.id.song_overflow);
        TextView songItemName = (TextView) songListLayout.findViewById(R.id.song_name);
        TextView songItemArtist = (TextView) songListLayout.findViewById(R.id.song_composer);

        // Set the parts equal to the corresponding song
        SongData songItem = songDataList.get(position);
        songItemIcon.setImageDrawable(songItem.getSongIcon());
        songItemName.setText(songItem.getSongName());
        songItemArtist.setText(songItem.getSongArtist());

        // Assertions
        Log.d(TAG, "Song Name: " + songItem.getSongName());
        Log.d(TAG, "Song Artist: " + songItem.getSongArtist());

        // Set listener if not selected
        if (!selected) {
            // Set on click listener for overflow
            songOverflowIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((BaseActivity) activity).showChoiceMenu(songListLayout, position);
                }
            });
        }

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
        selectSong(pos, !selectedSongs.contains(pos));
    }

    public void resetSelection() {
        selectedSongs = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void selectSong(Integer pos, boolean checked) {
        // Add if checked, remove if not checked
        if (checked) {
            selectedSongs.add(pos);
        } else {
            selectedSongs.remove(pos);
        }
        notifyDataSetChanged();
    }

    public int getSongCount() {
        Log.d(TAG, Integer.toString(selectedSongs.size()));
        return selectedSongs.size();
    }

    public List<Integer> getSelectedSongs() {
        return selectedSongs;
    }
}
