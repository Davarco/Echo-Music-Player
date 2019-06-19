package com.lunchareas.echomp.adapters;


import android.app.Activity;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.support.v7.widget.PopupMenu;
import android.widget.TextView;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.activities.MainActivity;
import com.lunchareas.echomp.dialogs.PlaylistRenameDialog;
import com.lunchareas.echomp.models.Playlist;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.MediaDataUtils;

import java.util.List;

public class PlaylistAdapter extends BaseAdapter {

    private List<Playlist> playlistList;
    private LayoutInflater layoutInflater;
    private Activity activity;

    public PlaylistAdapter(Activity activity, List<Playlist> playlistList) {
        this.playlistList = playlistList;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return playlistList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parentView) {

        // Inflate the layout
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_playlist, parentView, false);
            if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.gray_darker);
            } else {
                convertView.setBackgroundResource(R.color.gray);
            }

            // Get the parts
            TextView name = (TextView) convertView.findViewById(R.id.playlist_name);
            TextView count = (TextView) convertView.findViewById(R.id.playlist_count);
            ImageView overflow = (ImageView) convertView.findViewById(R.id.playlist_default_icon);

            // Set the parts
            Playlist playlist = playlistList.get(position);
            overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChoiceMenu(v, position);
                }
            });
            name.setText(playlist.getName());
            if (playlist.getCount() == 1) {
                String text = "1 Track";
                count.setText(text);
            } else if (playlist.getCount() == -1) {
                String text = "Smart Playlist";
                count.setText(text);
                if (playlist.getId() == Constants.LAST_ADDED_ID)
                    overflow.setImageResource(R.drawable.ic_last_added);
                else if (playlist.getId() == Constants.RECENTLY_PLAYED_ID)
                    overflow.setImageResource(R.drawable.ic_recently_played);
                else if (playlist.getId() == Constants.TOP_TRACKS_ID)
                    overflow.setImageResource(R.drawable.ic_top_tracks);
            } else {
                String text = playlist.getCount() + " Tracks";
                count.setText(text);
            }

            // Return the layout
            convertView.setTag(position);
            return convertView;
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        if (getCount() == 0) {
            return 1;
        }

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void showChoiceMenu(View view, final int pos) {
        final PopupMenu popupMenu = new PopupMenu(activity, view, Gravity.END);
        final Playlist playlist = playlistList.get(pos);

        // Handle individual clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.playlist_play_next: {
                        List<Song> songList = MediaDataUtils.getSongsFromPlaylist(playlist.getId(), activity);
                        MediaControlUtils.startQueue(activity, songList);
                        return true;
                    }
                    case R.id.playlist_rename: {
                        Bundle bundle = new Bundle();
                        bundle.putLong(PlaylistRenameDialog.PLAYLIST_ID, playlist.getId());
                        DialogFragment dialogFragment = new PlaylistRenameDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "");
                        return true;
                    }
                    case R.id.playlist_remove: {
                        MediaDataUtils.deletePlaylist(playlist.getId(), activity);
                        ((MainActivity) activity).updateAll();
                        return true;
                    }
                    case R.id.add_to_queue: {
                        MediaControlUtils.addToQueue(activity, MediaDataUtils.getSongsFromPlaylist(playlist.getId(), activity));
                        return true;
                    }
                }

                return false;
            }
        });

        // Create menu and show
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.choice_menu_playlist, popupMenu.getMenu());
        popupMenu.show();
    }
}
