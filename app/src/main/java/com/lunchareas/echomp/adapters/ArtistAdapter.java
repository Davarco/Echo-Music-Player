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
import com.lunchareas.echomp.dialogs.ArtistRenameDialog;
import com.lunchareas.echomp.models.Artist;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaControlUtils;
import com.lunchareas.echomp.utils.MediaDataUtils;

import java.util.List;

public class ArtistAdapter extends BaseAdapter {

    private List<Artist> artistList;
    private LayoutInflater layoutInflater;
    private Activity activity;

    public ArtistAdapter(Activity activity, List<Artist> artistList) {
        this.artistList = artistList;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return artistList.size();
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
            convertView = layoutInflater.inflate(R.layout.item_artist, parentView, false);
            if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.gray_darker);
            } else {
                convertView.setBackgroundResource(R.color.gray);
            }

            // Get the parts
            TextView name = (TextView) convertView.findViewById(R.id.artist_name);
            TextView count = (TextView) convertView.findViewById(R.id.artist_count);
            ImageView overflow = (ImageView) convertView.findViewById(R.id.artist_track_count);

            // Set the parts
            Artist artist = artistList.get(position);
            name.setText(artist.getName());
            if (artist.getAlbumCount() == 1) {
                String text = "1 Album";
                count.setText(text);
            } else {
                String text = artist.getAlbumCount() + " Albums";
                count.setText(text);
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
        final Artist artist = artistList.get(pos);

        // Handle individual clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.artist_play_next: {
                        MediaControlUtils.startQueue(activity, MediaDataUtils.getSongsFromArtist(artist.getId(), activity));
                        return true;
                    }
                    case R.id.artist_rename: {
                        Bundle bundle = new Bundle();
                        bundle.putLong(Constants.ARTIST_ID, artist.getId());
                        DialogFragment dialogFragment = new ArtistRenameDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "ArtistRename");
                    }
                    case R.id.add_to_queue: {
                        MediaControlUtils.addToQueue(activity, MediaDataUtils.getSongsFromArtist(artist.getId(), activity));
                        return true;
                    }
                }

                return false;
            }
        });

        // Create menu and show
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.choice_menu_artist, popupMenu.getMenu());
        popupMenu.show();
    }
}
