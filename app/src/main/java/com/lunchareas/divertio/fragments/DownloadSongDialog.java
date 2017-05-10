package com.lunchareas.divertio.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.activities.MainActivity;
import com.lunchareas.divertio.utils.SongUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private EditText composerNameInput;
    private String songName;
    private String userLink;
    private String composerName;
    private String downloadMusicLink;
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
        uploadDialogView = inflater.inflate(R.layout.upload_song_dialog, null);
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
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d(TAG, "Canceled MP3 upload.");
                }
            });

        return uploadBuilder.create();
    }

    private boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean hasActiveInternetConnection() {
        Log.d(TAG, "Reached internet connection checker...");
        if (isNetworkAvailable(getActivity())) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.something.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(3000);
                urlc.connect();
                Log.d(TAG, "Network is available, checking for response code...");
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(TAG, "No connection...");
            }
        } else {
            Log.e(TAG, "No network available...");
        }
        Log.d(TAG, "Apparently a network is available...");
        return false;
    }

    private void replaceDialogWithFailure() {
        ((MainActivity) getActivity()).createDownloadFailureDialog(this);
    }

    private void executeDialogDownload() {

        // Only go ahead if there is internet
        if (internetConnectionStatus) {

            // Get start
            long start = System.currentTimeMillis();

            // Get song name and link
            songNameInput = (EditText) uploadDialogView.findViewById(R.id.dialog_upload_name);
            userLinkInput = (EditText) uploadDialogView.findViewById(R.id.dialog_upload_link);
            composerNameInput = (EditText) uploadDialogView.findViewById(R.id.dialog_upload_composer);
            songName = songNameInput.getText().toString().trim();
            userLink = userLinkInput.getText().toString().trim();
            composerName = composerNameInput.getText().toString().trim();
            downloadMusicLink = "";
            songFileName = songName + ".mp3";
            Log.d(TAG, "Inserted link is " + userLink + ".");

            // Catch if name is empty
            if (!songName.equals("") && !songUtil.nameAlreadyExists(songName)) {

                // Download from activity
                ((MainActivity) getActivity()).downloadSong(userLink, songFileName, songName, composerName);

                // Using advanced api to get link line\
                /*
                try {
                    String downloadInfoLink = "http://api.youtube6download.top/api/?id=" + getYoutubeId(userLink);
                    Log.d(TAG, "The link is: " + downloadInfoLink);

                    // Use jsoup to find the download link
                    Document doc = Jsoup
                            .connect(downloadInfoLink)
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
                    Elements musicLinkElements = doc.getElementsByClass("q320");
                    while (musicLinkElements.size() == 0) {
                        doc = Jsoup
                                .connect(downloadInfoLink)
                                .header("Accept-Encoding", "gzip, deflate")
                                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                                .maxBodySize(0)
                                .timeout(6000)
                                .get();
                        musicLinkElements = doc.getElementsByClass("q320");
                        Thread.sleep(200);
                    }
                    downloadMusicLink = musicLinkElements.get(0).attr("href");
                    Log.d(TAG, "Final Download Link: " + downloadMusicLink);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                */

                // Close progress circle
                //((MainActivity) getActivity()).closeProgressCircle();

            } else {

                // Create dialog because name is invalid
                ((MainActivity) getActivity()).createNameFailureDialog(this);
            }

        } else {
            Log.d(TAG, "Could not connect to website?");
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

        Log.d(TAG, "Failed to get ID.");
        return null;
    }
}
