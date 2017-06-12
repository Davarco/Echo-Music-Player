package com.lunchareas.divertio.utils;

import android.content.Context;
import android.content.Intent;

import com.lunchareas.divertio.activities.PlayMediaService;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongData;

import java.util.Collections;
import java.util.List;

public class PlaylistQueueUtil {

    /*
    TODO: Need to test if playlist manager is actually going through all the songs
     */

    private static final String TAG = PlaylistQueueUtil.class.getName();

    private Context context;
    private List<SongData> songList;

    public PlaylistQueueUtil(PlaylistData playlistData, Context context) {
        this.context = context;
        this.songList = playlistData.getSongList();
    }

    public void startQueue() {
        // Log.d(TAG, "Starting queue!");
        sendMusicPauseIntent();
        sendPlaylistCreateIntent(songList);
    }

    public void sendPlaylistCreateIntent(List<SongData> songList) {
        // Log.d(TAG, "Trying to send playlist create intent.");
        Intent playlistCreateIntent = new Intent(context, PlayMediaService.class);

        // Shuffle list, get first song
        //SongData firstSong = songList.get(firstPos);
        Collections.shuffle(songList);

        /*
        // Switch 0th with supposed first song
        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getSongName().equals(firstSong.getSongName())) {
                Log.d(TAG, "Swapping " + i + " and " + 0 + ".");
                Collections.swap(songList, i, 0);
                break;
            } else {
                Log.d(TAG, "No swap.");
            }
        }
        */

        // Create the string array
        String[] songNameList = new String[songList.size()];
        for (int i = 0; i < songList.size(); i++) {
            songNameList[i] = songList.get(i).getSongName();
            // Log.d(TAG, "Song " + Integer.toString(i+1) + ": " + songNameList[i]);
        }

        // Send the intent
        playlistCreateIntent.setAction(PlayMediaService.PLAYLIST_CREATE);
        playlistCreateIntent.putExtra(PlayMediaService.PLAYLIST_CREATE, songNameList);
        context.startService(playlistCreateIntent);
    }

    public void sendMusicPauseIntent() {
        Intent musicPauseIntent = new Intent(context, PlayMediaService.class);
        musicPauseIntent.setAction(PlayMediaService.MUSIC_PAUSE);
        context.startService(musicPauseIntent);
    }

    public void sendMusicChangeIntent(int position) {
        Intent musicChangeIntent = new Intent(context, PlayMediaService.class);
        musicChangeIntent.putExtra(PlayMediaService.MUSIC_CHANGE, position);
        context.startService(musicChangeIntent);
    }
}
