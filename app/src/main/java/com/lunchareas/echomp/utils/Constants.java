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


public class Constants {

    // FONTS
    public static String FONT_PATH = "fonts/Nunito-Regular.ttf";

    // YT API KEY
    public static String YT_API_KEY = "AIzaSyAijzWQ0vdGfbO589uZar9WROvKQBnRf1Q";

    // Error codes
    public static final int MEDIA_ERROR = -1;

    // General constants
    public static final int HANDLER_DELAY = 300;

    // ID tags
    public static final String PLAYLIST_ID = "playlist_id";
    public static final String ALBUM_ID = "album_id";
    public static final String ARTIST_ID = "artist_id";
    public static final String SONG_ID = "song_id";
    public static final String SONG_LIST = "song_list";

    // Pager positions
    public static final int SONGS_POSITION = 0;
    public static final int PLAYLISTS_POSITION = 1;
    public static final int ALBUMS_POSITION = 2;
    public static final int ARTISTS_POSITION = 3;

    // Music supplementary constants
    public static final String MUSIC_RESULT = "request";
    public static final String MUSIC_POSITION = "position";
    public static final String MUSIC_DURATION = "duration";
    public static final String MUSIC_CURR = "current";
    public static final String MUSIC_LIST = "list";
    public static final String MUSIC_LIST_POS = "list_pos";
    public static final String MUSIC_ID = "music_id";

    // Music action constants
    public static final String MUSIC_PLAY = "play";
    public static final String MUSIC_INIT = "init";
    public static final String MUSIC_PAUSE = "pause";
    public static final String MUSIC_STOP = "stop";
    public static final String MUSIC_PREV = "prev";
    public static final String MUSIC_NEXT = "next";
    public static final String MUSIC_SHUFFLE = "shuffle";
    public static final String MUSIC_REPEAT = "repeat";
    public static final String MUSIC_CHANGE = "change";
    public static final String MUSIC_CREATE = "create";
    public static final String MUSIC_CREATE_REPEATING = "create_repeating";
    public static final String MUSIC_DEFAULT = "default";
    public static final String MUSIC_DEFAULT_REPEATING = "default_repeating";
    public static final String MUSIC_DEFAULT_REPEATING_SPECIFIC = "default_repeating_specific";
    public static final String MUSIC_DEFAULT_SHUFFLED = "default_shuffled";
    public static final String MUSIC_DEFAULT_REPEATING_SHUFFLED = "default_repeating_shuffled";
    public static final String MUSIC_ADD_TO_QUEUE = "add_to_queue";
    public static final String MUSIC_REMOVE_FROM_QUEUE = "remove_from_queue";

    // Result constants
    public static final int RESULT_CHOOSE_ALBUM_COVER = 1000;
    public static final int RESULT_UPDATE_ACTIVITY = 1001;
    public static final String RESULT_DEFAULT = "result";

    // Model constants
    public static final String SONG_NAME = "song_name";
    public static final String SONG_ARTIST = "song_artist";
    public static final String SONG_ALBUM = "song_album";
    public static final String PLAYLIST_OBJ = "playlist_obj";
    public static final String ALBUM_OBJ = "album_obj";
    public static final String ARTIST_OBJ = "artist_obj";

    // Playlist constants
    public static final long LAST_ADDED_ID = -1;
    public static final long RECENTLY_PLAYED_ID = -2;
    public static final long TOP_TRACKS_ID = -3;

    // Search constants
    public static final int SEARCH_TITLE = 0;
    public static final int SEARCH_SONG = 1;
    public static final int SEARCH_PLAYLIST = 2;
    public static final int SEARCH_ALBUM = 3;
    public static final int SEARCH_ARTIST = 4;
}
