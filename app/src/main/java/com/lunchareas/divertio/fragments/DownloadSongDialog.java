package com.lunchareas.divertio.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.utils.SongUtil;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadSongDialog extends DialogFragment {

    private static final String TAG = DownloadSongDialog.class.getName();

    private View uploadDialogView;
    private EditText songNameInput;
    private EditText userLinkInput;
    private String songName;
    private String userLink;
    private String songFileName;
    private boolean internetConnectionStatus;
    private SongUtil songUtil;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Create the song util
        songUtil = new SongUtil(getActivity());

        AlertDialog.Builder uploadBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        uploadDialogView = inflater.inflate(R.layout.dialog_download_song, null);
        uploadBuilder
            .setView(uploadDialogView)
            .setPositiveButton(R.string.download_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    // Check internet connection status
                    Thread checkConnectionThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            internetConnectionStatus = hasActiveInternetConnection();
                        }
                    });
                    checkConnectionThread.start();
                    try {
                        checkConnectionThread.join();
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't wait for connection thread.");
                    }

                    // Run the download procedure
                    executeDialogDownload();
                }
            })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });

        return uploadBuilder.create();
    }

    private boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean hasActiveInternetConnection() {
        if (isNetworkAvailable(getActivity())) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.something.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(3000);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(TAG, "No connection...");
            }
        } else {
            Log.e(TAG, "No network available...");
        }
        return false;
    }

    private void replaceDialogWithFailure() {
        ((MainActivity) getActivity()).createDownloadFailureDialog(this);
    }

    private void executeDialogDownload() {

        // Only go ahead if there is internet
        if (internetConnectionStatus) {

            // Get song name and link
            songNameInput = (EditText) uploadDialogView.findViewById(R.id.dialog_upload_name);
            userLinkInput = (EditText) uploadDialogView.findViewById(R.id.dialog_upload_link);
            songName = songNameInput.getText().toString().trim();
            userLink = userLinkInput.getText().toString().trim();

            // Set defaults
            if (songName == null || songName.equals("")) {
                songName = getYoutubeTitle(userLink);
            }

            // Get download link and file name
            songFileName = songName + ".mp3";
            // Log.d(TAG, "Inserted link is " + userLink + ".");

            // Replace occurrences of /
            songFileName = songFileName.replace('/', '|');

            // Catch if name is empty
            if (!songName.equals("") && !songUtil.nameAlreadyExists(songName)) {

                // Download from activity
                ((MainActivity) getActivity()).downloadSong(userLink, songFileName, songName);

            } else {

                // Create dialog because name is invalid
                ((MainActivity) getActivity()).createNameFailureDialog(this);
            }

        } else {
            Log.e(TAG, "Could not connect to website?");
            replaceDialogWithFailure();
        }
    }

    private String getYoutubeId(String link) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(link);
        if (matcher.find()) {
            return matcher.group();
        }

        Log.e(TAG, "Failed to get ID.");
        return null;
    }

    private String getYoutubeTitle(String url) {
        try {
            URL youtubeURL = new URL("http://www.youtube.com/oembed?url=" + url + "&format=json");
            return new JSONObject(IOUtils.toString(youtubeURL)).getString("title");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
