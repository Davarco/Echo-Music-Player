package com.lunchareas.echomp.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaDataUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

public class MusicPlayerFragment extends Fragment {

    private static final String TAG = MusicPlayerFragment.class.getName();

    private View view;
    private ImageView coverView;
    private TextView songName;
    private TextView songArtist;
    private TextView songAlbum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get the view
        view = inflater.inflate(R.layout.fragment_music_player, container, false);
        coverView = (ImageView) view.findViewById(R.id.song_cover);
        songName = (TextView) view.findViewById(R.id.song_name);
        songArtist = (TextView) view.findViewById(R.id.song_artist);
        songAlbum = (TextView) view.findViewById(R.id.album_name);

        // Get the data
        Bundle bundle = getArguments();
        long albumId = bundle.getLong(Constants.ALBUM_ID);
        String name = bundle.getString(Constants.SONG_NAME);
        String artist = bundle.getString(Constants.SONG_ARTIST);
        String album = bundle.getString(Constants.SONG_ALBUM);

        // Set the data
        Picasso.with(getActivity())
                .load(MediaDataUtils.getAlbumArt(albumId))
                .resize(512, 512)
                .onlyScaleDown()
                .centerInside()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .error(R.drawable.ic_album)
                .into(coverView);
        songName.setText(name);
        songArtist.setText(artist);
        songAlbum.setText(album);

        return view;
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(getActivity()));
        super.onDestroy();
    }
}
