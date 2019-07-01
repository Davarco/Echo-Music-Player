/*
Echo Music Player
Copyright (C) 2019 David Zhang

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lunchareas.echomp.adapters;


import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
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
import com.lunchareas.echomp.dialogs.PlaylistAddSongsDialog;
import com.lunchareas.echomp.dialogs.SongChangeAlbumDialog;
import com.lunchareas.echomp.dialogs.SongChangeArtistDialog;
import com.lunchareas.echomp.dialogs.SongRenameDialog;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.MediaDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class SongAdapter extends BaseAdapter {

    private static final String TAG = SongAdapter.class.getName();

    private List<Song> songList;
    private LayoutInflater layoutInflater;
    private Activity activity;

    public SongAdapter(Activity activity, List<Song> songList) {
        this.songList = songList;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return songList.size();
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

            // Get the layout
            convertView = layoutInflater.inflate(R.layout.item_song, parentView, false);
            if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.gray_darker);
            } else {
                convertView.setBackgroundResource(R.color.gray);
            }

            // Get the parts
            TextView name = (TextView) convertView.findViewById(R.id.song_name);
            TextView artist = (TextView) convertView.findViewById(R.id.song_artist);
            ImageView overflow = (ImageView) convertView.findViewById(R.id.song_duration);

            // Set the parts
            Song song = songList.get(position);
            name.setText(song.getName());
            String songDuration = String.format(
                    Locale.US, "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(song.getDuration()),
                    TimeUnit.MILLISECONDS.toSeconds(song.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(song.getDuration()))
            );
            artist.setText(song.getArtist() + " \u2022 " + songDuration);
            overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChoiceMenu(v, position);
                }
            });
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
        final Song song = songList.get(pos);

        // Handle individual clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.song_play: {
                        MediaControlUtils.init(activity, songList, pos);
                        ((MainActivity) activity).setMusicBound(true);
                        return true;
                    }
                    case R.id.song_rename: {
                        Bundle bundle = new Bundle();
                        bundle.putLong(Constants.SONG_ID, song.getId());
                        DialogFragment dialogFragment = new SongRenameDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "RenameSong");
                        return true;
                    }
                    case R.id.song_delete: {
                        MediaDataUtils.deleteSong(song.getId(), song.getPath(), activity);
                        ((MainActivity) activity).updateAll();
                        return true;
                    }
                    case R.id.song_album: {
                        Bundle bundle = new Bundle();
                        bundle.putLong(Constants.SONG_ID, song.getId());
                        DialogFragment dialogFragment = new SongChangeAlbumDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "ChangeSongAlbum");
                        return true;
                    }
                    case R.id.song_artist: {
                        Bundle bundle = new Bundle();
                        bundle.putLong(Constants.SONG_ID, song.getId());
                        DialogFragment dialogFragment = new SongChangeArtistDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "ChangeSongArtist");
                        return true;
                    }
                    case R.id.song_playlist: {
                        Bundle bundle = new Bundle();
                        ArrayList<Song> tempList = new ArrayList<>();
                        tempList.add(song);
                        bundle.putParcelableArrayList(Constants.SONG_LIST, tempList);
                        DialogFragment dialogFragment = new PlaylistAddSongsDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "AddToPlaylist");
                        return true;
                    }
                    case R.id.add_to_queue: {
                        MediaControlUtils.addToQueue(activity, song);
                        return true;
                    }
                }

                return false;
            }
        });

        // Create menu and show
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.choice_menu_song, popupMenu.getMenu());
        popupMenu.show();
    }
}
