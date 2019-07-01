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
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import com.lunchareas.echomp.adapters.AlbumAdapter;
import com.lunchareas.echomp.models.Album;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.NavUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class AlbumFragment extends Fragment {

    private static final String TAG = AlbumFragment.class.getName();

    private List<Album> albumList;
    private View albumView;
    private ListView listView;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get the data
        activity = getActivity();
        albumList = ((MainActivity) activity).getAlbumList();
        albumView = inflater.inflate(R.layout.fragment_album, container, false);

        // Set the list view to the songs
        listView = (ListView) albumView.findViewById(R.id.album_list);
        listView.setAdapter(new AlbumAdapter(activity, this, albumList));

        // Set single choice listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavUtils.goToAlbum(activity, albumList.get(position).getId());
            }
        });

        // Set background based on size
        if (albumList.size()%2 == 0) {
            albumView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray_darker);
        } else {
            albumView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray);
        }

        // Unique options menu
        setHasOptionsMenu(true);

        return albumView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu_album, menu);
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
                Collections.sort(((MainActivity) activity).getAlbumList(), new AlphabeticalComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.z_to_a: {
                Collections.sort(((MainActivity) activity).getAlbumList(), new ReverseAlphabeticalComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.artist: {
                Collections.sort(((MainActivity) activity).getAlbumList(), new ArtistComparator());
                ((MainActivity) activity).updateContent();
            }
            case R.id.count: {
                Collections.sort(((MainActivity) activity).getAlbumList(), new CountComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
        }

        return false;
    }

    private class AlphabeticalComparator implements Comparator<Album> {
        @Override
        public int compare(Album album1, Album album2) {
            return album1.getName().compareTo(album2.getName());
        }
    }

    private class ReverseAlphabeticalComparator implements Comparator<Album> {
        @Override
        public int compare(Album album1, Album album2) {
            return album2.getName().compareTo(album1.getName());
        }
    }

    private class ArtistComparator implements Comparator<Album> {
        @Override
        public int compare(Album album1, Album album2) {
            return album1.getArtist().compareTo(album2.getArtist());
        }
    }

    private class CountComparator implements Comparator<Album> {
        @Override
        public int compare(Album album1, Album album2) {
            return album1.getCount() - album2.getCount();
        }
    }
}
