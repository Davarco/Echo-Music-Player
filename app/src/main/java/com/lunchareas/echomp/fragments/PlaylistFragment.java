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
package com.lunchareas.echomp.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.activities.MainActivity;
import com.lunchareas.echomp.adapters.PlaylistAdapter;
import com.lunchareas.echomp.dialogs.PlaylistCreateDialog;
import com.lunchareas.echomp.models.Playlist;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.NavUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PlaylistFragment extends Fragment {

    private static final String TAG = PlaylistFragment.class.getName();

    private List<Playlist> playlistList;
    private View playlistView;
    private ListView listView;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get the data
        activity = getActivity();
        playlistList = ((MainActivity) activity).getPlaylistList();
        playlistView = inflater.inflate(R.layout.fragment_playlist, container, false);

        // Set the list view to the playlists
        listView = (ListView) playlistView.findViewById(R.id.playlist);
        listView.setAdapter(new PlaylistAdapter(activity, playlistList));

        // Set single choice listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavUtils.goToPlaylist(activity, playlistList.get(position).getId());
            }
        });

        // Set background based on size
        if (playlistList.size()%2 == 0) {
            playlistView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray_darker);
        } else {
            playlistView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray);
        }

        // Unique options menu
        setHasOptionsMenu(true);

        return playlistView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu_playlist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search: {
                NavUtils.goToSearch(activity);
                return true;
            }
            case R.id.play_all: {
                MediaControlUtils.startQueueRepeatingShuffled(activity, ((MainActivity) activity).getSongList());
                return true;
            }
            case R.id.a_to_z: {
                Collections.sort(((MainActivity) activity).getPlaylistList(), new AlphabeticalComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.z_to_a: {
                Collections.sort(((MainActivity) activity).getPlaylistList(), new ReverseAlphabeticalComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.count: {
                Collections.sort(((MainActivity) activity).getPlaylistList(), new CountComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.create_playlist: {
                DialogFragment dialogFragment = new PlaylistCreateDialog();
                dialogFragment.show(getFragmentManager(), "CreatePlaylist");
                return true;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
        }

        return false;
    }

    private class AlphabeticalComparator implements Comparator<Playlist> {
        @Override
        public int compare(Playlist playlist1, Playlist playlist2) {
            return playlist1.getName().compareTo(playlist2.getName());
        }
    }

    private class ReverseAlphabeticalComparator implements Comparator<Playlist> {
        @Override
        public int compare(Playlist playlist1, Playlist playlist2) {
            return playlist2.getName().compareTo(playlist1.getName());
        }
    }

    private class CountComparator implements Comparator<Playlist> {
        @Override
        public int compare(Playlist playlist1, Playlist playlist2) {
            return playlist1.getCount() - playlist2.getCount();
        }
    }
}
