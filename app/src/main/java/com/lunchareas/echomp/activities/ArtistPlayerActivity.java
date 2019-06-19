package com.lunchareas.echomp.activities;


import android.app.Activity;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lunchareas.echomp.R;
import com.lunchareas.echomp.adapters.SongPlainAdapter;
import com.lunchareas.echomp.dataloaders.ArtistLoader;
import com.lunchareas.echomp.models.Artist;
import com.lunchareas.echomp.models.Song;
import com.lunchareas.echomp.utils.Constants;
import com.lunchareas.echomp.utils.MediaDataUtils;
import com.lunchareas.echomp.utils.NavUtils;
import com.lunchareas.echomp.utils.ShareUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;


public class ArtistPlayerActivity extends ListPlayerActivity {

    private static final String TAG = ArtistPlayerActivity.class.getName();
    private final Activity activity = this;

    private Artist artist;

    @Override
    protected void getDispData() {

        // Get data model
        long id = getIntent().getLongExtra(Constants.ARTIST_ID, 0);
        artist = ArtistLoader.findArtistById(id);
    }

    @Override
    protected void showDispData() {

        // Extract data onto views
        if (artist != null) {

            // Set name and song list
            listName.setText(artist.getName());
            songList = MediaDataUtils.getSongsFromArtist(artist.getId(), activity);

            // Set cover using a random song
            Song song = songList.get((int)(Math.random()*songList.size()));
            Glide.with(activity)
                    .load(MediaDataUtils.getAlbumArt(song.getAlbumId()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(containerBackground);

            // Populate song list view
            songListView.setAdapter(new SongPlainAdapter(activity, songList));
            if (songList.size()%2 == 0) {
                containerBackground.setBackgroundResource(R.color.gray_darker);
            } else {
                containerBackground.setBackgroundResource(R.color.gray);
            }
        }
    }

    @Override
    protected void updateDispData() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.share: {
                ShareUtils.shareTrackList(activity, songList);
                break;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
            case R.id.search: {
                NavUtils.goToSearch(activity);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        System.gc();
        super.onDestroy();
    }
}
