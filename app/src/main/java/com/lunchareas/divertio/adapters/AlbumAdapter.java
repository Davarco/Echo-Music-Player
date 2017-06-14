package com.lunchareas.divertio.adapters;


import android.app.Activity;
import android.graphics.BitmapFactory;
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
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.models.SongData;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class AlbumAdapter extends BaseAdapter {

    private static final String TAG = AlbumAdapter.class.getName();

    private HashMap<String, List<SongData>> songAlbumList;
    private List<String> keyList;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    private Activity activity;

    public AlbumAdapter(Activity activity, HashMap<String, List<SongData>> songList, List<String> keys) {
        this.songAlbumList = songList;
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
        relativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.list_item_album, parentView, false);
        if (position % 2 - 1 == 0) {
            relativeLayout.setBackgroundResource(R.color.gray_2);
        } else {
            relativeLayout.setBackgroundResource(R.color.gray_3);
        }

        // Get parts of layout
        ImageView albumCover = (ImageView) relativeLayout.findViewById(R.id.album_cover);
        TextView albumName = (TextView) relativeLayout.findViewById(R.id.album_name);
        TextView albumSize = (TextView) relativeLayout.findViewById(R.id.album_size);

        // Get values
        String name = keyList.get(position);
        Drawable cover = songAlbumList.get(name).get(0).getSongCover();
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(activity.getResources(), ((BitmapDrawable) cover).getBitmap());
        drawable.setCornerRadius(50.0f);
        int size = songAlbumList.get(name).size();

        // Set values
        albumCover.setImageDrawable(drawable);
        albumName.setText(name);
        if (size == 1) {
            albumSize.setText("1 song");
        } else {
            albumSize.setText(Integer.toString(size) + " songs");
        }

        // Return
        relativeLayout.setTag(position);
        return relativeLayout;
    }
}
