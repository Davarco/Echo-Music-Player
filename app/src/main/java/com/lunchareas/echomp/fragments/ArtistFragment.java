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
import com.lunchareas.echomp.adapters.ArtistAdapter;
import com.lunchareas.echomp.models.Artist;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.NavUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ArtistFragment extends Fragment {

    private List<Artist> artistList;
    private View artistView;
    private ListView listView;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get the data
        activity = getActivity();
        artistList = ((MainActivity) activity).getArtistList();
        artistView = inflater.inflate(R.layout.fragment_artist, container, false);

        // Set the list view to the songs
        listView = (ListView) artistView.findViewById(R.id.artist_list);
        listView.setAdapter(new ArtistAdapter(activity, artistList));

        // Set single choice listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavUtils.goToArtist(activity, artistList.get(position).getId());
            }
        });

        // Set background based on size
        if (artistList.size()%2 == 0) {
            artistView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray_darker);
        } else {
            artistView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray);
        }

        // Unique options menu
        setHasOptionsMenu(true);

        return artistView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu_artist, menu);
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
                Collections.sort(((MainActivity) activity).getArtistList(), new ArtistFragment.AlphabeticalComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.z_to_a: {
                Collections.sort(((MainActivity) activity).getArtistList(), new ArtistFragment.ReverseAlphabeticalComparator());
                ((MainActivity) activity).updateContent();
                return true;
            }
            case R.id.count: {
                Collections.sort(((MainActivity) activity).getArtistList(), new ArtistFragment.CountComparator());
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

    private class AlphabeticalComparator implements Comparator<Artist> {
        @Override
        public int compare(Artist artist1, Artist artist2) {
            return artist1.getName().compareTo(artist2.getName());
        }
    }

    private class ReverseAlphabeticalComparator implements Comparator<Artist> {
        @Override
        public int compare(Artist artist1, Artist artist2) {
            return artist2.getName().compareTo(artist1.getName());
        }
    }

    private class CountComparator implements Comparator<Artist> {
        @Override
        public int compare(Artist artist1, Artist artist2) {
            return artist1.getAlbumCount() - artist2.getAlbumCount();
        }
    }
}
