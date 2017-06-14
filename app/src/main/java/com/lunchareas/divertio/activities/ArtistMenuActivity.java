package com.lunchareas.divertio.activities;


import android.app.Activity;
import android.widget.ListView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.adapters.ArtistAdapter;
import com.lunchareas.divertio.models.SongData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArtistMenuActivity extends BaseListActivity {

    private static final String TAG = ArtistMenuActivity.class.getName();

    private HashMap<String, List<SongData>> songArtistList;
    private List<String> keyList;
    private ListView listView;

    public ArtistMenuActivity() {
        super(R.layout.activity_artist_menu);
    }

    @Override
    protected void initList() {

        // Get list view
        ArtistMenuActivity.this.setTitle("Artists");
        listView = (ListView) findViewById(R.id.artist_list);
        setMainView();
    }

    @Override
    public void setMainView() {
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Get the songs
                findSongsByArtist();
                ArtistAdapter artistAdapter = new ArtistAdapter(activity, songArtistList, keyList);
                listView.setAdapter(artistAdapter);

                // Set correct background color
                if (keyList.size() % 2 - 1 == 0) {
                    findViewById(R.id.activity).setBackgroundResource(R.color.gray_2);
                } else {
                    findViewById(R.id.activity).setBackgroundResource(R.color.gray_3);
                }
            }
        });
    }

    private void findSongsByArtist() {

        // Create key list
        keyList = new ArrayList<>();
        songArtistList = new HashMap<>();

        // Go through songs
        List<SongData> songList = getSongInfoList();
        for (SongData songData: songList) {
            String artist = songData.getSongArtist();

            // Create new if album does not exist
            if (!keyList.contains(artist)) {
                keyList.add(artist);
                songArtistList.put(artist, new ArrayList<SongData>());
            }
            songArtistList.get(artist).add(songData);
        }
    }
}
