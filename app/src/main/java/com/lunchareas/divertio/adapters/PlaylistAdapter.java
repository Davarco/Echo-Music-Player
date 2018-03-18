package com.lunchareas.divertio.adapters;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lunchareas.divertio.activities.BaseListActivity;
import com.lunchareas.divertio.activities.PlaylistMenuActivity;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.R;

import java.util.List;

public class PlaylistAdapter extends BaseAdapter {

    private List<PlaylistData> playlistDataList;
    private LayoutInflater playlistInflater;
    private Activity activity;

    public PlaylistAdapter(Activity activity, List<PlaylistData> playlist) {
        this.playlistDataList = playlist;
        this.playlistInflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return playlistDataList.size();
    }

    // Not used
    @Override
    public Object getItem(int arg0) {
        return null;
    }

    // Not used
    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parentView) {
        if (convertView == null) {
            convertView = playlistInflater.inflate(R.layout.list_item_playlist, parentView, false);
            if (position % 2 - 1 == 0) {
                convertView.setBackgroundResource(R.color.gray_2);
            } else {
                convertView.setBackgroundResource(R.color.gray_3);
            }

            // Get the parts of a playlist layout
            //ImageView playlistItemIcon = (ImageView) convertView.findViewById(R.id.playlist_icon);
            ImageView playlistOverflowIcon = (ImageView) convertView.findViewById(R.id.playlist_overflow);
            TextView playlistItemName = (TextView) convertView.findViewById(R.id.playlist_name);
            TextView playlistItemSize = (TextView) convertView.findViewById(R.id.playlist_size);

            // Set the parts equal to the corresponding playlist
            PlaylistData playlistItem = playlistDataList.get(position);
            //playlistItemIcon.setImageDrawable(playlistItem.getPlaylistIcon());
            playlistItemName.setText(playlistItem.getPlaylistName());
            if (playlistItem.getNumSongs() != 1) {
                playlistItemSize.setText(Integer.toString(playlistItem.getNumSongs()) + " songs");
            } else {
                playlistItemSize.setText(Integer.toString(playlistItem.getNumSongs()) + " song");
            }

            // Set on click listener for overflow
            playlistOverflowIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((PlaylistMenuActivity) activity).showChoiceMenu(v, position);
                }
            });

            // Set position as tag
            convertView.setTag(position);
            return convertView;
        }

        return convertView;
    }
}
