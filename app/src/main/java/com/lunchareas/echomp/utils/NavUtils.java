/*
Echo Music Player
Copyright (C) 2019 David Zhang

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lunchareas.echomp.utils;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.activities.AlbumPlayerActivity;
import com.lunchareas.echomp.activities.ArtistPlayerActivity;
import com.lunchareas.echomp.activities.EqualizerActivity;
import com.lunchareas.echomp.activities.MusicPlayerActivity;
import com.lunchareas.echomp.activities.PlaylistPlayerActivity;
import com.lunchareas.echomp.activities.SearchActivity;
import com.lunchareas.echomp.dataloaders.SongLoader;
import com.lunchareas.echomp.models.Album;
import com.lunchareas.echomp.models.Artist;
import com.lunchareas.echomp.models.Playlist;
import com.lunchareas.echomp.models.Song;


public class NavUtils {

    public static final String TAG = NavUtils.class.getName();

    public static void goToSong(Activity activity, long id) {
        Intent intent = new Intent(activity, MusicPlayerActivity.class);
        intent.putExtra(Constants.MUSIC_ID, id);
        activity.startActivity(intent);
    }

    public static void goToPlaylist(Activity activity, long id) {
        Intent intent = new Intent(activity, PlaylistPlayerActivity.class);
        intent.putExtra(Constants.PLAYLIST_ID, id);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivityForResult(intent, Constants.RESULT_UPDATE_ACTIVITY);
    }

    public static void goToArtist(Activity activity, long id) {
        Intent intent = new Intent(activity, ArtistPlayerActivity.class);
        intent.putExtra(Constants.ARTIST_ID, id);
        activity.startActivity(intent);
    }

    public static void goToAlbum(Activity activity, long id) {
        Intent intent = new Intent(activity, AlbumPlayerActivity.class);
        intent.putExtra(Constants.ALBUM_ID, id);
        activity.startActivity(intent);
    }

    public static void goToObject(Activity activity, Object object) {
        if (object instanceof Song) {
            MediaControlUtils.initRepeating(activity, SongLoader.songList, SongLoader.songList.indexOf(object));
            NavUtils.goToSong(activity, ((Song) object).getId());
        } else if (object instanceof Playlist) {
            Log.e(TAG, "Going to playlist.");
            NavUtils.goToPlaylist(activity, ((Playlist) object).getId());
        } else if (object instanceof Album) {
            Log.e(TAG, "Going to album.");
            NavUtils.goToAlbum(activity, ((Album) object).getId());
        } else if (object instanceof Artist) {
            Log.e(TAG, "Going to artist.");
            NavUtils.goToArtist(activity, ((Artist) object).getId());
        } else {
            Log.e(TAG, "Could not determine object type.");
        }
    }

    public static void goToSearch(Activity activity) {
        Intent intent = new Intent(activity, SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    public static void goToEqualizer(Activity activity) {
        Intent intent = new Intent(activity, EqualizerActivity.class);
        activity.startActivity(intent);
    }
}
