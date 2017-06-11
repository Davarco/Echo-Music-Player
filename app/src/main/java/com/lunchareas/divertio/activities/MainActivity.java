package com.lunchareas.divertio.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.content.*;

import com.lunchareas.divertio.adapters.SongSelectionAdapter;
import com.lunchareas.divertio.fragments.ImportPromptDialog;
import com.lunchareas.divertio.fragments.ImportSongDialog;
import com.lunchareas.divertio.fragments.AddToPlaylistDialog;
import com.lunchareas.divertio.fragments.ChangeSongArtistDialog;
import com.lunchareas.divertio.fragments.ChangeSongTitleDialog;
import com.lunchareas.divertio.fragments.CreatePlaylistFromSongsDialog;
import com.lunchareas.divertio.fragments.DeleteSongDialog;
import com.lunchareas.divertio.R;
import com.lunchareas.divertio.adapters.SongAdapter;
import com.lunchareas.divertio.fragments.DownloadNameFailureDialog;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.fragments.DownloadSongDialog;
import com.lunchareas.divertio.fragments.DownloadConnectionFailureDialog;
import com.lunchareas.divertio.utils.SongUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.lang.*;

public class MainActivity extends BaseListActivity {

    private static final String TAG = MainActivity.class.getName();

    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 501;
    public static final String MUSIC_DIR_NAME = "Divertio";

    private ListView songView;
    private SongSelectionAdapter selectionAdapter;
    private ProgressDialog progressDialog;
    private ProgressDialog downloadProgress;
    private DownloadTask downloadTask;
    private Thread downloadThread;
    private String downloadMusicLink;

    /*
    Shared preferences for first run only.
     */
    private SharedPreferences sharedPreferences;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // External storage permissions
        if (Build.VERSION.SDK_INT < 23) {
            Log.d(TAG, "Don't need permissions.");
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d(TAG, "PERMISSIONS: App needs permissions to read external storage.");
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

        // Create directory for files if it does not exist
        File musicFolder = new File(Environment.getExternalStorageDirectory() + File.separator + MUSIC_DIR_NAME);
        if (!musicFolder.exists()) {
            musicFolder.mkdir();
        }

        // Get shared prefs
        sharedPreferences = getSharedPreferences("com.lunchareas.divertio", MODE_PRIVATE);
    }

    @Override
    protected void initList() {

        // Get song info and set the listview
        songView = (ListView) findViewById(R.id.song_list);
        setMainView();

        // Create the selection adapter
        if (songInfoList == null) {
            Log.d(TAG, "No song list found yet.");
        }
        selectionAdapter = new SongSelectionAdapter(this, R.layout.list_item_song, songInfoList);

        // Add single click listener
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Pause and change icon
                songCtrlButton.setBackgroundResource(R.drawable.ic_pause);
                String name = songInfoList.get(position).getSongName();
                sendMusicPauseIntent();
                sendMusicCreateIntent(name);
                musicBound = true;

                // Start new song activity
                Intent i = new Intent(getApplicationContext(), MusicActivity.class);
                i.putExtra(MusicActivity.MUSIC_NAME, songInfoList.get(position).getSongName());
                startActivity(i);
                finish();
            }
        });

        // Set new mode and add listener
        songView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        songView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Change the title to num of clicked items
                Log.d(TAG, "Song item checked state changed.");
                int numChecked = songView.getCheckedItemCount();
                mode.setTitle(numChecked + " Selected");
                selectionAdapter.toggleSelection(position);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Create the menu for the overflow
                Log.d(TAG, "Creating song action mode.");
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.main_selection_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Set colored and hide bar
                Log.d(TAG, "Preparing song action mode.");
                resetAdapter();
                songView.setAdapter(selectionAdapter);
                //getSupportActionBar().hide();
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                Log.d(TAG, "Song action item clicked.");
                switch (item.getItemId()) {
                    case R.id.song_selection_delete: {
                        // Delete the selected songs
                        Log.d(TAG, "Deleting these songs!");
                        songUtil = new SongUtil(context);
                        songUtil.deleteSongList(getSongsFromIndexes(selectionAdapter.getSelectedSongs()));
                        setMainView();
                        mode.finish();
                        return true;
                    }
                    case R.id.song_selection_create_playlist: {
                        // Create a playlist with the songs
                        Log.d(TAG, "Creating a playlist.");
                        DialogFragment createPlaylistDialog = new CreatePlaylistFromSongsDialog();
                        Bundle bundle = new Bundle();
                        bundle.putIntegerArrayList(CreatePlaylistFromSongsDialog.MUSIC_LIST, (ArrayList<Integer>) selectionAdapter.getSelectedSongs());
                        createPlaylistDialog.setArguments(bundle);
                        createPlaylistDialog.show(getSupportFragmentManager(), "CreatePlaylistFromSongs");
                        mode.finish();
                        return true;
                    }
                    case R.id.song_selection_add_to: {
                        // Add songs to playlists
                        Log.d(TAG, "Adding these songs to a playlist!");
                        DialogFragment addToPlaylistDialog = new AddToPlaylistDialog();
                        Bundle bundle = new Bundle();
                        bundle.putIntegerArrayList(AddToPlaylistDialog.MUSIC_LIST, (ArrayList<Integer>) selectionAdapter.getSelectedSongs());
                        addToPlaylistDialog.setArguments(bundle);
                        addToPlaylistDialog.show(getSupportFragmentManager(), "AddToPlaylists");
                        mode.finish();
                        return true;
                    }
                    case R.id.song_selection_reset: {
                        // Reset song selections
                        Log.d(TAG, "Resetting selections!");
                        selectionAdapter.resetSelection();
                        mode.setTitle("0 Selected");
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                Log.d(TAG, "Song action mode destroyed.");
                selectionAdapter.resetSelection();
                getSupportActionBar().show();
            }
        });
    }

    @SuppressLint("NewApi")
    public void showChoiceMenu(View view, final int pos) {
        final PopupMenu popupMenu = new PopupMenu(context, view, Gravity.END);
        final SongData selectedSong = songInfoList.get(pos);

        // Handle individual clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.song_play: {

                        // Pause and change icon
                        songCtrlButton.setBackgroundResource(R.drawable.ic_pause);
                        String name = songInfoList.get(pos).getSongName();
                        sendMusicPauseIntent();
                        sendMusicCreateIntent(name);
                        musicBound = true;

                        return true;
                    }
                    case R.id.song_rename_title: {
                        Log.d(TAG, "Renaming song title!");

                        // Create popup for new title
                        DialogFragment changeSongTitleDialog = new ChangeSongTitleDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString(ChangeSongTitleDialog.MUSIC_POS, songInfoList.get(pos).getSongName());
                        changeSongTitleDialog.setArguments(bundle);
                        changeSongTitleDialog.show(getSupportFragmentManager(), "ChangeTitle");

                        return true;
                    }
                    case R.id.song_delete_title: {
                        Log.d(TAG, "Deleting song!");

                        // Remove song from list and re-update view
                        SongUtil songController = new SongUtil(context);
                        songController.deleteSong(selectedSong);
                        setMainView();

                        return true;
                    }
                    case R.id.song_change_artist: {
                        Log.d(TAG, "Changing song artist!");

                        // Create popup for new artist
                        DialogFragment changeSongArtistDialog = new ChangeSongArtistDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString(ChangeSongArtistDialog.MUSIC_POS, songInfoList.get(pos).getSongName());
                        changeSongArtistDialog.setArguments(bundle);
                        changeSongArtistDialog.show(getSupportFragmentManager(), "ChangeArtist");

                        return true;
                    }
                    case R.id.song_to_playlist: {
                        Log.d(TAG, "Adding song to playlist!");

                        // Create popup to add to playlist
                        DialogFragment addToPlaylistDialog = new AddToPlaylistDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString(AddToPlaylistDialog.MUSIC_POS, songInfoList.get(pos).getSongName());
                        addToPlaylistDialog.setArguments(bundle);
                        addToPlaylistDialog.show(getSupportFragmentManager(), "AddSongToPlaylist");

                        return true;
                    }
                    default: {
                        return false;
                    }
                }
            }
        });

        // Create menu and show
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.main_choice_menu, popupMenu.getMenu());
        popupMenu.show();
    }

    public void createDownloadFailureDialog(DialogFragment d) {
        d.dismiss();
        DialogFragment uploadFailureDialog = new DownloadConnectionFailureDialog();
        uploadFailureDialog.show(getSupportFragmentManager(), "UploadFailure");
    }

    public void createDownloadFailureDialog() {
        DialogFragment uploadFailureDialog = new DownloadConnectionFailureDialog();
        uploadFailureDialog.show(getSupportFragmentManager(), "UploadFailure");
    }

    public void createNameFailureDialog(DialogFragment d) {
        d.dismiss();
        DialogFragment nameFailureDialog = new DownloadNameFailureDialog();
        nameFailureDialog.show(getSupportFragmentManager(), "NameFailure");
    }

    public void createNameFailureDialog() {
        DialogFragment nameFailureDialog = new DownloadNameFailureDialog();
        nameFailureDialog.show(getSupportFragmentManager(), "NameFailure");
    }

    public void createImportDialog() {
        DialogFragment dialogFragment = new ImportSongDialog();
        dialogFragment.show(getSupportFragmentManager(), "Import");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Detected that position " + item.getItemId() + " was selected.");
        switch (item.getItemId()) {
            case R.id.song_menu_add: {
                Log.d(TAG, "Starting new dialog - add.");
                DialogFragment addSongDialog = new ImportSongDialog();
                addSongDialog.show(getSupportFragmentManager(), "Add");
                return true;
            }
            case R.id.song_menu_upload: {
                Log.d(TAG, "Starting new dialog - download.");
                DialogFragment uploadDialog = new DownloadSongDialog();
                uploadDialog.show(getSupportFragmentManager(), "Upload");
                return true;
            }
            case R.id.song_menu_delete: {
                Log.d(TAG, "Starting new dialog - delete.");
                DialogFragment deleteDialog = new DeleteSongDialog();
                deleteDialog.show(getSupportFragmentManager(), "Delete");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Options for drawer menu
    @Override
    protected void selectMenuItem(int position) {
        Log.d(TAG, "Detected click on position " + position + ".");
        switch (position) {
            case 0: {
                Log.d(TAG, "No effect, on that activity!");
                break;
            }
            case 1: {
                Log.d(TAG, "Starting new activity - playlist.");
                Intent i = new Intent(this, PlaylistMenuActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;
            }
            case 2: {
                Log.d(TAG, "Starting new activity - now playing.");
                Intent i = new Intent(this, MusicActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.putExtra(MusicActivity.MUSIC_NAME, currSong);
                startActivity(i);
                break;
            }
            /*
            case 2: {
                Log.d(TAG, "Starting new activity - bluetooth.");
                Intent i = new Intent(this, BluetoothActivity.class);
                startActivity(i);
                break;
            }
            case 3: {
                Log.d(TAG, "Starting new activity - settings.");
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            }
            */
        }
    }

    @Override
    public void setMainView() {
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Setting main view.");
                updateSongInfoList();
                SongAdapter songListAdapter = new SongAdapter(activity, songInfoList);
                songView.setAdapter(songListAdapter);
            }
        });
    }

    public void addProgressCircle() {
        // Create a spinning wheel
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Adding progress circle.");
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("Trying to get song...");
                progressDialog.setIndeterminate(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        });
    }

    public void closeProgressCircle() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Closing progress circle.");
                progressDialog.dismiss();
            }
        });
    }

    public void openDownloadProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadProgress = new ProgressDialog(MainActivity.this);
                downloadProgress.setMessage("Converting song...");
                downloadProgress.setIndeterminate(true);
                downloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                downloadProgress.setCancelable(false);
                downloadProgress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadTask.cancel(false);
                        downloadProgress.dismiss();
                    }
                });
            }
        });
    }

    /*
    This needs to be entirely rewritten if there is time.
     */
    public void downloadSong(final String userLink, final String songFileName, final String songName) {

        downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {

                // Add spinning circle
                addProgressCircle();

                try {
                    String downloadInfoLink = "https://www.youtubeinmp3.com/download/?video=" + userLink;
                    Log.d(TAG, "The link is: " + downloadInfoLink);

                    // Use jsoup to find the download link
                    Document doc = Jsoup.connect(downloadInfoLink)
                            .header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                            .maxBodySize(0)
                            .timeout(6000)
                            .get();
                    if (doc == null) {
                        Log.d(TAG, "The doc is empty.");
                    } else {
                        Log.d(TAG, "The doc is not empty.");
                    }
                    Element musicLinkElement = doc.getElementById("download");
                    downloadMusicLink = "https://youtubeinmp3.com" + musicLinkElement.attr("href");
                    Log.d(TAG, "Final Download Link: " + downloadMusicLink);

                } catch (Exception e) {
                    e.printStackTrace();
                    closeProgressCircle();
                    createDownloadFailureDialog();
                    return;
                }

                // Close the circle, open bar
                closeProgressCircle();

                // Open download progress
                openDownloadProgress();

                // Download the song
                downloadTask = new DownloadTask(MainActivity.this, songFileName, songName);
                downloadTask.execute(downloadMusicLink);
            }
        });
        downloadThread.start();
    }

    // Not going to use for now, but could be useful later on
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "Service is running.");
                return true;
            }
        }
        Log.d(TAG, "Service is not running.");
        return false;
    }

    private void resetAdapter() {
        selectionAdapter = new SongSelectionAdapter(this, R.layout.list_item_song, songInfoList);
    }

    /*
    Class that manages download.
     */
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private String songFileName;
        private String songName;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context, String songFileName, String songName) {
            this.context = context;
            this.songFileName = songFileName;
            this.songName = songName;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // Download the file
                int fileLength = connection.getContentLength();
                input = connection.getInputStream();
                output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/Divertio/" + songFileName);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadProgress.show();
                }
            });
        }

        @Override
        protected void onProgressUpdate(final Integer... progress) {
            super.onProgressUpdate(progress);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadProgress.setIndeterminate(false);
                    downloadProgress.setMax(100);
                    downloadProgress.setProgress(progress[0]);
                    if (progress[0] > 0) {
                        downloadProgress.setMessage("Downloading song...");
                    }
                }
            });
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadProgress.dismiss();
                }
            });

            // Get metadata
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(Environment.getExternalStorageDirectory().getPath() + "/Divertio/" + songFileName);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String path = Environment.getExternalStorageDirectory().getPath() + "/Divertio/" + songFileName;
            Drawable cover = Drawable.createFromStream(new ByteArrayInputStream(retriever.getEmbeddedPicture()), null);

            // Change artist if empty
            if (artist == null || artist.equals("")) {
                artist = "Unknown";
            }

            // Add to database
            SongData songData = new SongData(songName, path, artist, cover);
            SongDBHandler db = new SongDBHandler(getApplicationContext());
            db.addSongData(songData);

            // Reset the song list view
            setMainView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Run first run if necessary
        if (sharedPreferences.getBoolean("firstrun", true)) {

            // Import initial
            DialogFragment dialogFragment = new ImportPromptDialog();
            dialogFragment.show(getSupportFragmentManager(), "ImportInitial");

            // Change shared prefs
            sharedPreferences.edit().putBoolean("firstrun", false).apply();
        }
    }
}
