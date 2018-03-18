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
import com.lunchareas.divertio.activities.BaseListActivity;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.models.SongData;

import java.util.ArrayList;
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
        if (convertView == null) {
            // Base color on selection
            boolean selected = selectedSongs.contains(position);
            if (selected) {
                convertView = songListInflater.inflate(R.layout.list_item_song_selected, parentView, false);
                if (position % 2 - 1 == 0) {
                    convertView.setBackgroundResource(R.color.gray_2);
                } else {
                    convertView.setBackgroundResource(R.color.gray_3);
                }
            } else {
                convertView = songListInflater.inflate(R.layout.list_item_song, parentView, false);
            }

            // Get the parts of a song layout
            ImageView songOverflowIcon = (ImageView) convertView.findViewById(R.id.song_overflow);
            TextView songItemName = (TextView) convertView.findViewById(R.id.song_name);
            TextView songItemArtist = (TextView) convertView.findViewById(R.id.song_composer);

            // Set the parts equal to the corresponding song
            SongData songItem = songDataList.get(position);
            songItemName.setText(songItem.getSongName());
            songItemArtist.setText(songItem.getSongArtist());

            // Set listener if not selected
            if (!selected) {
                // Set on click listener for overflow
                songOverflowIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) activity).showChoiceMenu(v, position);
                    }
                });
            }

            // Set position as tag
            convertView.setTag(position);
            return convertView;
        }

        return convertView;
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
        return selectedSongs.size();
    }

    public List<Integer> getSelectedSongs() {
        return selectedSongs;
    }
}
