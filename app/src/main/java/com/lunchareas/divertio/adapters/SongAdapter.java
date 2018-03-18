package com.lunchareas.divertio.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseListActivity;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.models.SongData;

import java.util.List;

public class SongAdapter extends BaseAdapter {

    private static final String TAG = SongAdapter.class.getName();

    private List<SongData> songDataList;
    private LayoutInflater songListInflater;
    private Activity activity;

    public SongAdapter(Activity activity, List<SongData> songList) {
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
    public View getView(final int position, View convertView, ViewGroup parentView) {
        if (convertView == null) {
            convertView = songListInflater.inflate(R.layout.list_item_song, parentView, false);
            if (position % 2 - 1 == 0) {
                convertView.setBackgroundResource(R.color.gray_2);
            } else {
                convertView.setBackgroundResource(R.color.gray_3);
            }

            // Get the parts of a song layout
            //ImageView songItemIcon = (ImageView) convertView.findViewById(R.id.song_icon);
            ImageView songOverflowIcon = (ImageView) convertView.findViewById(R.id.song_overflow);
            TextView songItemName = (TextView) convertView.findViewById(R.id.song_name);
            TextView songItemArtist = (TextView) convertView.findViewById(R.id.song_composer);

            // Set the parts equal to the corresponding song
            SongData songItem = songDataList.get(position);
            //songItemIcon.setImageDrawable(songItem.getSongCover());
            songItemName.setText(songItem.getSongName());
            songItemArtist.setText(songItem.getSongArtist());

            // Set on click listener for overflow
            songOverflowIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) activity).showChoiceMenu(v, position);
                }
            });

            // Set position as tag
            convertView.setTag(position);
            return convertView;
        }

        return convertView;
    }
}
