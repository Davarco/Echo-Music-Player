package com.lunchareas.divertio.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.models.SongData;

import java.util.List;

public class SongFixedAdapter extends BaseAdapter {

    private static final String TAG = SongFixedAdapter.class.getName();

    private List<SongData> songDataList;
    private LayoutInflater songListInflater;
    private Activity activity;

    public SongFixedAdapter(Activity activity, List<SongData> songList) {
        this.songDataList = songList;
        this.songListInflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return songDataList.size();
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
        final RelativeLayout songListLayout = (RelativeLayout) songListInflater.inflate(R.layout.song_fixed_layout, parentView, false);

        // Get the parts of a song layout
        ImageView songItemIcon = (ImageView) songListLayout.findViewById(R.id.song_icon);
        TextView songItemName = (TextView) songListLayout.findViewById(R.id.song_name);
        TextView songItemArtist = (TextView) songListLayout.findViewById(R.id.song_composer);

        // Set the parts equal to the corresponding song
        SongData songItem = songDataList.get(position);
        songItemIcon.setImageDrawable(songItem.getSongIcon());
        songItemName.setText(songItem.getSongName());
        songItemArtist.setText(songItem.getSongArtist());

        // Assertions
        Log.d(TAG, songItem.toString());

        // Set position as tag
        songListLayout.setTag(position);
        return songListLayout;
    }
}
