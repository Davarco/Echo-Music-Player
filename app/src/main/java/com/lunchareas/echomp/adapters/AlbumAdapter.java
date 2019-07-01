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
import android.net.Uri;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import com.lunchareas.echomp.dialogs.AlbumRenameDialog;
import com.lunchareas.echomp.models.Album;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.MediaDataUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;


public class AlbumAdapter extends BaseAdapter {

    private static final String TAG = AlbumAdapter.class.getName();

    private List<Album> albumList;
    private LayoutInflater layoutInflater;
    private Activity activity;
    private Fragment fragment;

    public AlbumAdapter(Activity activity, Fragment fragment, List<Album> albumList) {
        this.albumList = albumList;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return albumList.size();
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
            convertView = layoutInflater.inflate(R.layout.item_album, parentView, false);
            if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.gray_darker);
            } else {
                convertView.setBackgroundResource(R.color.gray);
            }

            // Get the parts
            ImageView cover = (ImageView) convertView.findViewById(R.id.album_cover);
            TextView name = (TextView) convertView.findViewById(R.id.album_name);
            TextView artist = (TextView) convertView.findViewById(R.id.album_artist);
            ImageView overflow = (ImageView) convertView.findViewById(R.id.album_count);

            // Set the cover
            Long id = albumList.get(position).getId();
            Uri uri = MediaDataUtils.getAlbumArt(id);
            Picasso.with(activity)
                    .load(uri)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resize(128, 128)
                    .transform(new CropCircleTransformation())
                    .error(R.drawable.ic_album)
                    .into(cover);

            // Set the parts
            Album album = albumList.get(position);
            name.setText(album.getName());
            if (album.getCount() != 1) {
                artist.setText(album.getArtist() + " \u2022 " + album.getCount() + " Tracks");
            } else {
                artist.setText(album.getArtist() + " \u2022 " + album.getCount() + " Track");
            }
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
        final Album album = albumList.get(pos);

        // Handle individual clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.album_play_next: {
                        MediaControlUtils.startQueue(activity, MediaDataUtils.getSongsFromAlbum(album.getId(), activity));
                        return true;
                    }
                    case R.id.album_rename: {
                        Bundle bundle = new Bundle();
                        bundle.putLong(Constants.ALBUM_ID, album.getId());
                        DialogFragment dialogFragment = new AlbumRenameDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "AlbumRename");
                        return true;
                    }
                    case R.id.album_cover_change: {
                        ((MainActivity) activity).choosePhoto(album.getId());
                        return true;
                    }
                    case R.id.add_to_queue: {
                        MediaControlUtils.addToQueue(activity, MediaDataUtils.getSongsFromAlbum(album.getId(), activity));
                        return true;
                    }
                }

                return false;
            }
        });

        // Create menu and show
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.choice_menu_album, popupMenu.getMenu());
        popupMenu.show();
    }
}
