package com.lunchareas.divertio.adapters;


import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.models.SongData;

import java.util.HashMap;
import java.util.List;

public class ArtistAdapter extends BaseAdapter {

    private static final String TAG = ArtistAdapter.class.getName();

    private HashMap<String, List<SongData>> songArtistList;
    private List<String> keyList;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    private Activity activity;

    public ArtistAdapter(Activity activity, HashMap<String, List<SongData>> songList, List<String> keys) {
        this.songArtistList = songList;
        this.keyList = keys;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return keyList.size();
    }

    // Not used
    @Override
    public Object getItem(int arg0) {
        return null;
    }

    // Not used
    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parentView) {
        relativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.list_item_artist, parentView, false);
        if (position % 2 - 1 == 0) {
            relativeLayout.setBackgroundResource(R.color.gray_2);
        } else {
            relativeLayout.setBackgroundResource(R.color.gray_3);
        }

        // Get parts of layout
        TextView artistName = (TextView) relativeLayout.findViewById(R.id.artist_name);
        TextView artistSize = (TextView) relativeLayout.findViewById(R.id.artist_size);

        // Get values
        String name = keyList.get(position);
        int size = songArtistList.get(name).size();

        // Set values
        artistName.setText(name);
        if (size == 1) {
            artistSize.setText("1 song");
        } else {
            artistSize.setText(Integer.toString(size) + " songs");
        }

        // Return
        relativeLayout.setTag(position);
        return relativeLayout;
    }
}
