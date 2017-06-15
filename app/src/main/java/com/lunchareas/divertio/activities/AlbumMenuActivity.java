package com.lunchareas.divertio.activities;


import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.adapters.AlbumAdapter;
import com.lunchareas.divertio.models.SongData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlbumMenuActivity extends BaseListActivity {

    private static final String TAG = AlbumMenuActivity.class.getName();

    private HashMap<String, List<SongData>> songAlbumList;
    private List<String> keyList;
    private ListView listView;

    public AlbumMenuActivity() {
        super(R.layout.activity_album_menu);
    }

    @Override
    protected void initList() {

        // Get list view
        AlbumMenuActivity.this.setTitle("Albums");
        listView = (ListView) findViewById(R.id.album_song_list);
        setMainView();

        // Detect clicks
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        // For feeling
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
    }

    @Override
    public void setMainView() {
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Get the songs
                findSongsByAlbum();
                AlbumAdapter albumAdapter = new AlbumAdapter(activity, songAlbumList, keyList);
                listView.setAdapter(albumAdapter);

                // Set correct background color
                if (keyList.size() % 2 - 1 == 0) {
                    findViewById(R.id.activity).setBackgroundResource(R.color.gray_2);
                } else {
                    findViewById(R.id.activity).setBackgroundResource(R.color.gray_3);
                }
            }
        });
    }

    private void findSongsByAlbum() {

        // Create key list
        keyList = new ArrayList<>();
        songAlbumList = new HashMap<>();

        // Go through songs
        List<SongData> songList = getSongInfoList();
        for (SongData songData: songList) {
            String album = songData.getSongAlbum();

            // Create new if album does not exist
            if (!keyList.contains(album)) {
                keyList.add(album);
                songAlbumList.put(album, new ArrayList<SongData>());
            }
            songAlbumList.get(album).add(songData);
        }
    }
}
