package com.lunchareas.divertio.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.lunchareas.divertio.activities.PlaylistActivity;
import com.lunchareas.divertio.models.PlaylistData;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSelectionAdapter extends ArrayAdapter<PlaylistData> {

    private static final String TAG = PlaylistSelectionAdapter.class.getName();

    private List<PlaylistData> playlistDataList;
    private LayoutInflater playlistInflater;
    private Context activity;
    private List<Integer> selectedPlaylists;

    public PlaylistSelectionAdapter(Activity activity, int resourceId, List<PlaylistData> playlistDataList) {
        super(activity, resourceId, playlistDataList);
        this.playlistDataList = playlistDataList;
        this.playlistInflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.selectedPlaylists = new ArrayList<>();
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parentView) {

        // Base color on selection
        boolean selected = selectedPlaylists.contains(position);
        final RelativeLayout playlistLayout;
        if (selected) {
            playlistLayout = (RelativeLayout) playlistInflater.inflate(R.layout.playlist_selected_layout, parentView, false);
        } else {
            playlistLayout = (RelativeLayout) playlistInflater.inflate(R.layout.playlist_layout, parentView, false);
        }
        final RelativeLayout playlistListLayout = playlistLayout;
        
        // Get the parts of a playlist layout
        ImageView playlistItemIcon = (ImageView) playlistLayout.findViewById(R.id.playlist_icon);
        ImageView playlistOverflowIcon = (ImageView) playlistLayout.findViewById(R.id.playlist_overflow);
        TextView playlistItemName = (TextView) playlistLayout.findViewById(R.id.playlist_name);
        TextView playlistItemSize = (TextView) playlistLayout.findViewById(R.id.playlist_size);

        // Set the parts equal to the corresponding playlist
        PlaylistData playlistItem = playlistDataList.get(position);
        playlistItemIcon.setImageDrawable(playlistItem.getPlaylistIcon());
        playlistItemName.setText(playlistItem.getPlaylistName());
        playlistItemSize.setText(playlistItem.getNumSongs());

        // Set listener if not selected
        if (!selected) {
            // Set on click listener for overflow
            playlistOverflowIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((BaseActivity) activity).showChoiceMenu(playlistLayout, position);
                }
            });
        }

        // Set position as tag
        playlistLayout.setTag(position);
        return playlistLayout;
    }

    @Override
    public void remove(PlaylistData playlistData) {
        playlistDataList.remove(playlistData);
        notifyDataSetChanged();
    }

    public void toggleSelection(int pos) {
        // !contains returns false if already exists
        selectPlaylist(pos, !playlistDataList.contains(pos));
    }

    public void resetSelection() {
        selectedPlaylists = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void selectPlaylist(Integer pos, boolean checked) {
        // Add if checked, remove if not checked
        if (checked) {
            selectedPlaylists.add(pos);
        } else {
            selectedPlaylists.remove(pos);
        }
        notifyDataSetChanged();
    }

    public int getSongCount() {
        Log.d(TAG, Integer.toString(selectedPlaylists.size()));
        return selectedPlaylists.size();
    }

    public List<Integer> getSelectedPlaylists() {
        return selectedPlaylists;
    }
}
