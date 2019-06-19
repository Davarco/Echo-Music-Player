package com.lunchareas.echomp.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.lunchareas.echomp.R;
import com.lunchareas.echomp.utils.MusicDownloadUtils;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongDownloadDialog extends DialogFragment {

    private static final String TAG = SongDownloadDialog.class.getName();

    private View uploadDialogView;
    private EditText userLinkInput;
    private String userLink;
    private String songFileName;
    private String songName;
    private boolean internetConnectionStatus;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        uploadDialogView = View.inflate(getActivity(), R.layout.dialog_song_download, null);
        builder.setView(uploadDialogView);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
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
            });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });

        return builder.create();
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
                Log.e(TAG, "No connection.");
            }
        } else {
            Log.e(TAG, "No network available.");
        }
        return false;
    }

    private void executeDialogDownload() {

        // Only go ahead if there is internet
        if (internetConnectionStatus) {

            // Get song name and link
            userLinkInput = (EditText) uploadDialogView.findViewById(R.id.download_link);
            userLink = userLinkInput.getText().toString().trim();

            // Get download link and file name
            getYoutubeTitle(userLink);
            songFileName = songName + ".mp3";

            // Replace occurrences of /
            // songFileName = songFileName.replace(' ', '_');
            songFileName = songFileName.replace('/', '-');
            songFileName = songFileName.replace('|', '-');
            songFileName = songFileName.replace(':', '-');
            songFileName = songFileName.replace('?', '-');
            songFileName = songFileName.replace('*', '-');
            songFileName = songFileName.replace('<', '-');
            songFileName = songFileName.replace('>', '-');
            songFileName = songFileName.replace('\"', '-');
            songFileName = songFileName.replace('\\', '-');

            // Begin download
            if (songName != null) {
                MusicDownloadUtils.downloadFromYoutube(userLink, songFileName, getActivity());
            }
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

    private void getYoutubeTitle(final String url) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL youtubeURL = new URL("http://www.youtube.com/oembed?url=" + url + "&format=json");
                    songName = new JSONObject(IOUtils.toString(youtubeURL)).getString("title");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
