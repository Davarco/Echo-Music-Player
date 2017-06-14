package com.lunchareas.divertio.activities;


import android.app.Activity;
import android.widget.ListView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.adapters.GenreAdapter;
import com.lunchareas.divertio.models.SongData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenreMenuActivity extends BaseListActivity {

    private static final String TAG = GenreMenuActivity.class.getName();

    private HashMap<String, List<SongData>> songGenreList;
    private List<String> keyList;
    private ListView listView;

    public GenreMenuActivity() {
        super(R.layout.activity_genre_menu);
    }

    @Override
    protected void initList() {

        // Get list view
        listView = (ListView) findViewById(R.id.genre_list);
        setMainView();
    }

    @Override
    public void setMainView() {
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Get the songs
                findSongsByGenre();
                GenreAdapter genreAdapter = new GenreAdapter(activity, songGenreList, keyList);
                listView.setAdapter(genreAdapter);

                // Set correct background color
                if (keyList.size() % 2 - 1 == 0) {
                    findViewById(R.id.activity).setBackgroundResource(R.color.gray_2);
                } else {
                    findViewById(R.id.activity).setBackgroundResource(R.color.gray_3);
                }
            }
        });
    }
    
    private void findSongsByGenre() {

        // Create key list
        keyList = new ArrayList<>();
        songGenreList = new HashMap<>();

        // Go through songs
        List<SongData> songList = getSongInfoList();
        for (SongData songData: songList) {
            String genre = songData.getSongGenre();

            // Create new if genre does not exist
            if (!keyList.contains(genre)) {
                keyList.add(genre);
                songGenreList.put(genre, new ArrayList<SongData>());
            }
            songGenreList.get(genre).add(songData);
        }
    }
}
