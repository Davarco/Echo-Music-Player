package com.lunchareas.echomp.adapters;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class QueueSelectionAdapter extends ArrayAdapter<Song> {

    private static final String TAG = QueueSelectionAdapter.class.getName();

    private List<Song> songList;
    private LayoutInflater layoutInflater;
    private Context activity;
    private List<Integer> selectedSongs;

    public QueueSelectionAdapter(Activity activity, int resourceId, List<Song> songList) {
        super(activity, resourceId, songList);
        this.songList = songList;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.selectedSongs = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parentView) {

        if (convertView == null) {

            // Base color on selection
            convertView = layoutInflater.inflate(R.layout.item_queue, parentView, false);
            boolean selected = selectedSongs.contains(position);
            if (selected) {
                convertView.setBackgroundResource(R.color.blue);
                convertView.setAlpha(0.7f);
            } else {
                if (position % 2 == 0) {
                    convertView.setBackgroundResource(R.color.gray_darker);
                } else {
                    convertView.setBackgroundResource(R.color.gray);
                }
            }

            // Get the parts of a queue layout
            ImageView cover = (ImageView) convertView.findViewById(R.id.album_cover);
            TextView name = (TextView) convertView.findViewById(R.id.song_name);
            TextView artist = (TextView) convertView.findViewById(R.id.song_artist);
            VuMeterView vumeter = (VuMeterView) convertView.findViewById(R.id.vumeter);

            // Set cover
            Uri uri = MediaDataUtils.getAlbumArt(songList.get(position).getAlbumId());
            if (cover.getDrawable() == null) {
                Picasso.with(activity)
                        .load(uri)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .resize(128, 128)
                        .transform(new CropCircleTransformation())
                        .error(R.drawable.ic_album)
                        .into(cover);
            }

            // Set text
            name.setText(songList.get(position).getName());
            String songDuration = String.format(
                    Locale.US, "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(songList.get(position).getDuration()),
                    TimeUnit.MILLISECONDS.toSeconds(songList.get(position).getDuration()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(position).getDuration()))
            );
            artist.setText(songList.get(position).getArtist() + " \u2022 " + songDuration);

            // Set vumeter
            if (songList.get(position).getId() == MediaService.getCurrSong()) {
                vumeter.resume(true);
            } else {
                vumeter.stop(false);
                vumeter.pause();
            }

        } else {

            // Base color on selection
            boolean selected = selectedSongs.contains(position);

            // Set color and alpha
            if (selected) {
                convertView.setBackgroundResource(R.color.blue);
                convertView.setAlpha(0.7f);
            } else {
                if (position % 2 == 0) {
                    convertView.setBackgroundResource(R.color.gray_darker);
                } else {
                    convertView.setBackgroundResource(R.color.gray);
                }
            }
        }

        return convertView;
    }

    @Override
    public void remove(Song songData) {
        songList.remove(songData);
        notifyDataSetChanged();
    }

    public void toggleSelection(int pos) {
        // !contains returns false if it already exists
        selectSong(pos, !selectedSongs.contains(pos));
    }

    public void resetSelection() {
        selectedSongs = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void selectSong(Integer pos, boolean checked) {
        // Add if checked, remove if not checked
        if (checked) {
            selectedSongs.add(pos);
        } else {
            selectedSongs.remove(pos);
        }
        notifyDataSetChanged();
    }

    public int getSongCount() {
        return selectedSongs.size();
    }

    public List<Integer> getSelectedSongs() {
        return selectedSongs;
    }
}
