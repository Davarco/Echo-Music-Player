package com.lunchareas.divertio.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
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
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.activities.MainActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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
        ((MainActivity)getActivity()).replaceDialogWithFailure(this);
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

            long input = System.currentTimeMillis();

            // Using advanced api to get link line
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
            }

            // Get jsoup time
            long jsoup = System.currentTimeMillis();

            // Replace with error dialog if this fails
            DownloadManager.Request youtubeConvertRequest;
            try {
                // Insert link into api and setup download
                youtubeConvertRequest = new DownloadManager.Request(Uri.parse(downloadMusicLink));
                youtubeConvertRequest.setDescription("Converting and downloading...");
                youtubeConvertRequest.setTitle(songFileName + " Download");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    youtubeConvertRequest.allowScanningByMediaScanner();
                    youtubeConvertRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }
            } catch (Exception e) {
                replaceDialogWithFailure();
                return;
            }

            // Download into music files directory
            youtubeConvertRequest.setDestinationInExternalPublicDir("/Divertio", songFileName);
            DownloadManager youtubeConvertManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            youtubeConvertManager.enqueue(youtubeConvertRequest);

            // Get download time
            long download = System.currentTimeMillis();

            // Update database
            String musicFilePath = Environment.getExternalStorageDirectory().getPath() + "/Divertio/" + songFileName;
            SongDBHandler db = new SongDBHandler(getActivity());
            try {
                SongData songData = new SongData(songName, musicFilePath, composerName);
                Log.d(TAG, "Composer name: " + composerName);
                db.addSongData(songData);
                Log.d(TAG, "Successfully updated song database.");
            } catch (Exception e) {
                Log.d(TAG, "Song database update failure.");
            }

            // Get end time
            long end = System.currentTimeMillis();

            // Reset the song list view
            ((MainActivity) getActivity()).setSongListView();

            // Print times
            Log.d(TAG, "Total: " + Long.toString(end-start));
            Log.d(TAG, "Input: " + Long.toString(input-start));
            Log.d(TAG, "JSOUP: " + Long.toString(jsoup-input));
            Log.d(TAG, "Download: " + Long.toString(download-jsoup));
            Log.d(TAG, "Database: " + Long.toString(end-jsoup));

        } else {
            Log.d(TAG, "Could not connect to website?");
            replaceDialogWithFailure();
        }
    }
}
