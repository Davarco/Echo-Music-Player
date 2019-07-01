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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.services.MediaService;
import com.lunchareas.echomp.utils.MediaDataUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.gresse.hugo.vumeterlibrary.VuMeterView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;


public class QueueAdapter extends BaseAdapter {

    private static final String TAG = QueueAdapter.class.getName();

    private List<Song> songList;
    private LayoutInflater layoutInflater;
    private Activity activity;

    public QueueAdapter(Activity activity, List<Song> songList) {
        this.songList = songList;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
        if (this.songList == null) {
            this.songList = new ArrayList<>();
        }
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
        return songList.get(arg0).getId();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parentView) {

        ViewHolder viewHolder;

        // Inflate the layout
        if (convertView == null) {

            // Get the view
            convertView = layoutInflater.inflate(R.layout.item_queue, parentView, false);
            viewHolder = new ViewHolder();
            viewHolder.vuMeterView = (VuMeterView) convertView.findViewById(R.id.vumeter);
            if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.gray_darker);
            } else {
                convertView.setBackgroundResource(R.color.gray);
            }

            // Get the parts
            viewHolder.cover = (ImageView) convertView.findViewById(R.id.album_cover);
            viewHolder.name = (TextView) convertView.findViewById(R.id.song_name);
            viewHolder.artist = (TextView) convertView.findViewById(R.id.song_artist);

            // Load image
            viewHolder.albumId = songList.get(position).getAlbumId();

            // Set tag
            convertView.setTag(viewHolder);

        } else {

            // No need to reinstate view holder
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.albumId = songList.get(position).getAlbumId();
        }

        // Set data
        if (songList.get(position).getId() == MediaService.getCurrSong()) {
            viewHolder.vuMeterView.resume(true);
        } else {
            viewHolder.vuMeterView.stop(false);
            viewHolder.vuMeterView.pause();
        }
        viewHolder.name.setText(songList.get(position).getName());
        String songDuration = String.format(
                Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(songList.get(position).getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(songList.get(position).getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(position).getDuration()))
        );
        viewHolder.artist.setText(songList.get(position).getArtist() + " \u2022 " + songDuration);
        Uri uri = MediaDataUtils.getAlbumArt(viewHolder.albumId);
        Picasso.with(activity)
                .load(uri)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resize(128, 128)
                .transform(new CropCircleTransformation())
                .error(R.drawable.ic_album)
                .into(viewHolder.cover);

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

    public void updateContent() {
        songList = MediaService.getSongList();
        notifyDataSetChanged();
    }

    private class ViewHolder {
        private VuMeterView vuMeterView;
        private ImageView cover;
        private TextView name;
        private TextView artist;
        private long albumId;
    }
}
