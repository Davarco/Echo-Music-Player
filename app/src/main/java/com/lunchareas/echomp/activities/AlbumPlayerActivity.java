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
package com.lunchareas.echomp.activities;


import android.app.Activity;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lunchareas.echomp.R;
import com.lunchareas.echomp.adapters.SongPlainAdapter;
import com.lunchareas.echomp.dataloaders.AlbumLoader;
import com.lunchareas.echomp.models.Album;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaDataUtils;
import com.lunchareas.echomp.utils.NavUtils;
import com.lunchareas.echomp.utils.ShareUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;


public class AlbumPlayerActivity extends ListPlayerActivity {

    private static final String TAG = AlbumPlayerActivity.class.getName();
    private final Activity activity = this;

    private Album album;

    @Override
    protected void getDispData() {

        // Get data model
        long id = getIntent().getLongExtra(Constants.ALBUM_ID, 0);
        album = AlbumLoader.findAlbumById(id);
    }

    @Override
    protected void showDispData() {

        // Extract data onto views
        if (album != null) {

            // Set name and song list
            listName.setText(album.getName());
            songList = MediaDataUtils.getSongsFromAlbum(album.getId(), activity);

            // Set cover using a random song
            Song song = songList.get((int)(Math.random()*songList.size()));
            Glide.with(activity)
                    .load(MediaDataUtils.getAlbumArt(song.getAlbumId()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(containerBackground);

            // Populate song list view
            songListView.setAdapter(new SongPlainAdapter(activity, songList));
            if (songList.size()%2 == 0) {
                containerBackground.setBackgroundResource(R.color.gray_darker);
            } else {
                containerBackground.setBackgroundResource(R.color.gray);
            }
        }
    }

    @Override
    protected void updateDispData() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.share: {
                ShareUtils.shareTrackList(activity, songList);
                break;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
            case R.id.search: {
                NavUtils.goToSearch(activity);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        System.gc();
        super.onDestroy();
    }
}
