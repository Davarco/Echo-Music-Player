package com.lunchareas.divertio;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends BaseAdapter {

    private List<PlaylistData> playlistDataList;
    private LayoutInflater playlistInflater;

    public PlaylistAdapter(Context c, List<PlaylistData> playlist) {
        this.playlistDataList = playlist;
        this.playlistInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return playlistDataList.size();
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
        RelativeLayout playlistLayout = (RelativeLayout) playlistInflater.inflate(R.layout.playlist_layout, parentView, false);

        // get the parts of a playlist layout
        ImageView playlistItemIcon = (ImageView) playlistLayout.findViewById(R.id.playlist_icon);
        TextView playlistItemName = (TextView) playlistLayout.findViewById(R.id.playlist_name);
        TextView playlistItemSize = (TextView) playlistLayout.findViewById(R.id.playlist_size);

        // set the parts equal to the corresponding playlist
        PlaylistData playlistItem = playlistDataList.get(position);
        playlistItemIcon.setImageDrawable(playlistItem.getPlaylistIcon());
        playlistItemName.setText(playlistItem.getPlaylistName());
        playlistItemSize.setText(Integer.toString(playlistItem.getNumSongs()) + " songs");

        // set position as tag
        playlistLayout.setTag(position);
        return playlistLayout;
    }
}
