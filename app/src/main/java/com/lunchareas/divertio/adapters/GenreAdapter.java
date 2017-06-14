package com.lunchareas.divertio.adapters;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.models.SongData;

import java.util.HashMap;
import java.util.List;

public class GenreAdapter extends BaseAdapter {

    private static final String TAG = GenreAdapter.class.getName();

    private HashMap<String, List<SongData>> songGenreList;
    private List<String> keyList;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    private Activity activity;

    public GenreAdapter(Activity activity, HashMap<String, List<SongData>> songList, List<String> keys) {
        this.songGenreList = songList;
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
        relativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.list_item_genre, parentView, false);
        if (position % 2 - 1 == 0) {
            relativeLayout.setBackgroundResource(R.color.gray_2);
        } else {
            relativeLayout.setBackgroundResource(R.color.gray_3);
        }

        // Get parts of layout
        TextView genreName = (TextView) relativeLayout.findViewById(R.id.genre_name);
        TextView genreSize = (TextView) relativeLayout.findViewById(R.id.genre_size);

        // Get values
        String name = keyList.get(position);
        int size = songGenreList.get(name).size();

        // Set values
        genreName.setText(name);
        if (size == 1) {
            genreSize.setText("1 song");
        } else {
            genreSize.setText(Integer.toString(size) + " songs");
        }

        // Return
        relativeLayout.setTag(position);
        return relativeLayout;
    }
}
