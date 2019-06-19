package com.lunchareas.echomp.utils;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lunchareas.echomp.activities.MainActivity;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicDownloadUtils {

    private static final String TAG = MusicDownloadUtils.class.getName();

    private static final String options[] = new String[]{"320 kbps", "256 kbps", "192 kbps", "128 kbps", "64 kbps"};

    private static ProgressDialog convertProgressDialog;
    private static ProgressDialog downloadProgressDialog;
    private static AlertDialog.Builder builder;

    private static String fileName;
    private static String fileLink;
    private static String downloadLink;
    private static String json;

    public static void downloadFromYoutube(final String userLink, final String songFileName, final Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Get the download link
                fileName = songFileName;
                fileLink = userLink;
                downloadLink = "";

                // Add spinning circle
                addProgressCircle(activity);

                try {

                    // Connect to URL
                    Log.e(TAG, "Starting MP3 download..");
                    // URL jsonUrl = new URL("https://youtubetoany.com/@api/json/mp3/" + getYoutubeId(userLink));
                    // URL jsonUrl = new URL("https://www.youtubetmp3.co/@api/json/mp3/" + getYoutubeId(userLink));
                    // HttpURLConnection request = (HttpURLConnection) jsonUrl.openConnection();
                    // request.connect();

                    // Fix JSON
                    /*
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(new InputStreamReader((InputStream) request.getContent()), writer);
                    json = writer.toString();
                    int idx = json.indexOf('<');
                    json = json.substring(0, idx);
                    */
                    // Create new dialog to prompt bitrate
                    builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Select Bitrate:");
                    builder.setSingleChoiceItems(options, 0, null);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            beginDownload(((AlertDialog) dialog).getListView().getCheckedItemPosition(), json, activity);
                        }
                    });
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog dialog = builder.create();
                            closeProgressCircle(activity);
                            dialog.show();
                        }
                    });

                    /*
                    partially working api?
                    String downloadInfoLink = "https://youtube-mp3.to/convert.php?file=mp3&quality=max&apiEntry=" + userLink;

                    // Use jsoup to find the download link
                    Document doc = Jsoup.connect(downloadInfoLink)
                            .header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                            .maxBodySize(0)
                            .timeout(6000)
                            .get();
                    // Log.e(TAG, doc.toString());
                    Elements musicLinkElements = doc.getElementsByClass("btn-info");
                    for (Element element: musicLinkElements) {
                        downloadLink = element.attr("href");
                    }
                    Log.e(TAG, downloadLink);
                    */

                    /*
                    youtube in mp3 api
                    String downloadInfoLink = "https://www.youtubeinmp3.com/download/?video=" + userLink;

                    // Use jsoup to find the download link
                    Document doc = Jsoup.connect(downloadInfoLink)
                            .header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                            .maxBodySize(0)
                            .timeout(6000)
                            .get();
                    Element musicLinkElement = doc.getElementById("download");
                    downloadLink = "https://youtubeinmp3.com" + musicLinkElement.attr("href");
                    */

                    /*
                    String downloadInfoLink = "https://youtube-mp3.to/convert.php?file=mp3&quality=max&apiEntry=" + userLink;

                    // Use jsoup to find the download link
                    Document doc = Jsoup.connect(downloadInfoLink)
                            .header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                            .maxBodySize(0)
                            .timeout(10000)
                            .get();
                    Elements musicLinkElements = doc.getElementsByClass("btn-info");
                    for (Element element: musicLinkElements) {
                        downloadLink = element.attr("href");
                    }
                    */

                    /*
                    break tv api
                    for (int i = 1; i < 6; i++) {
                        String temp = "http://d" + Integer.toString(i) + ".ytcore.org/widget/dl.php?idv=" + getYoutubeId(userLink) +
                                "&type=mp3&qu=best&title=" + getYoutubeTitle(userLink) + "&server=http://d" + Integer.toString(i) + ".ytcore.org/";
                        Log.e(TAG, temp);
                        URL url = new URL(temp);
                        // Log.e(TAG, url.toString());
                        URLConnection con = url.openConnection();
                        con.setDoOutput(true);

                        // Turn string into json
                        InputStream stream = con.getInputStream();
                        JSONObject object = getJsonObject(stream);
                        if (object != null) {
                            Log.e(TAG, Integer.toString(i) + " success");
                            downloadLink = "http://d" + Integer.toString(i) + ".ytcore.org/sse/?jobid=" + object.getString("success");
                            break;
                        } else {
                            Log.e(TAG, Integer.toString(i) + " failed");
                        }
                    }
                    */

                } catch (Exception e) {
                    e.printStackTrace();
                    closeProgressCircle(activity);
                }

            }
        }).start();
    }

    private static void beginDownload(final int option, String json, final Activity activity) {

        // Convert to json object
        /*
        Log.e(TAG, Integer.toString(option));
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(json);
        JsonObject object = root.getAsJsonObject();
        downloadLink = object.getAsJsonObject("vidInfo").getAsJsonObject(Integer.toString(option)).get("dloadUrl").toString();
        downloadLink = "https://" + downloadLink.substring(3);
        Log.e(TAG, downloadLink);
        */

        /*
        // Open download progress
        DownloadTask downloadTask = new DownloadTask(activity, songFileName, activity);
        openDownloadProgress(activity, downloadTask);

        // Download the song
        downloadTask.execute(downloadLink);
        */


        final String downloadInfoLink = "http://youtube-mp3.info/yt-api.php?id=" + getYoutubeId(fileLink);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Use jsoup to find the download link
                    Document doc = Jsoup.connect(downloadInfoLink)
                            .header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                            .maxBodySize(0)
                            .timeout(6000)
                            .get();
                    System.out.println(doc);
                    // Log.e(TAG, doc.toString());
                    Elements musicLinkElements = doc.getElementsByClass("link");
                    downloadLink = musicLinkElements.get(option).child(0).attr("href");
                    Log.e(TAG, "Link: " + downloadLink);

                    // Set request for download
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadLink));
                    request.setDescription("Downloading song...");
                    request.setTitle(fileName);

                    // Set download destination
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

                    // Send download request, register receiver for finish
                    DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                    manager.enqueue(request);
                    activity.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                } catch (Exception e) { e.printStackTrace(); }
            }
        }).start();

    }

    private static void addProgressCircle(final Activity activity) {
        // Create a spinning wheel
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                convertProgressDialog = new ProgressDialog(activity);
                convertProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                convertProgressDialog.setMessage("Converting song...");
                convertProgressDialog.setIndeterminate(true);
                convertProgressDialog.setCanceledOnTouchOutside(false);
                convertProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        convertProgressDialog.dismiss();
                    }
                });
                convertProgressDialog.show();
            }
        });
    }

    private static void closeProgressCircle(Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                convertProgressDialog.dismiss();
            }
        });
    }

    private static void openDownloadProgress(final Activity activity, final DownloadTask downloadTask) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadProgressDialog = new ProgressDialog(activity);
                downloadProgressDialog.setMessage("Downloading song...");
                downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                downloadProgressDialog.setCancelable(false);
                downloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadTask.cancel(false);
                        downloadProgressDialog.dismiss();
                    }
                });
            }
        });
    }

    /*
    Class that manages download.
     */
    private static class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private String songFileName;
        private PowerManager.WakeLock mWakeLock;
        private Activity activity;
        private String path;

        private DownloadTask(Context context, String songFileName, Activity activity) {
            this.context = context;
            this.songFileName = songFileName;
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input;
            OutputStream output;
            HttpURLConnection connection;
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
                path = Environment.getExternalStorageDirectory() + File.separator +
                        Environment.DIRECTORY_DOWNLOADS + File.separator + songFileName;
                output = new FileOutputStream(path);

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
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadProgressDialog.show();
                }
            });
        }

        @Override
        protected void onProgressUpdate(final Integer... progress) {
            super.onProgressUpdate(progress);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadProgressDialog.setIndeterminate(false);
                    downloadProgressDialog.setMax(100);
                    downloadProgressDialog.setProgress(progress[0]);
                    if (progress[0] > 0) {
                        downloadProgressDialog.setMessage("Downloading song...");
                    }
                }
            });
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadProgressDialog.dismiss();
                }
            });

            // Reset the song list view
            MediaScannerConnection.scanFile(activity,
                    new String[] { path }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path2, Uri uri) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
                                    ((MainActivity) activity).updateAll();
                                    if (mp == null) {
                                        // TODO: Add error message!
                                        Log.e(TAG, "Download failed.");
                                    }
                                }
                            });
                        }
                    });
        }
    }

    public static String getYoutubeId(String link) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(link);
        if (matcher.find()) {
            return matcher.group();
        }

        Log.e(TAG, "Failed to get ID.");
        return null;
    }

    private static String getYoutubeTitle(final String url) {

        try {
            URL youtubeURL = new URL("http://www.youtube.com/oembed?url=" + url + "&format=json");
            String title = new JSONObject(IOUtils.toString(youtubeURL)).getString("title");
            title = title.replace('_', ' ');
            return title;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static JSONObject getJsonObject(InputStream inputStreamObject) {

        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStreamObject, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            String json = responseStrBuilder.toString();

            return new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Log.e(TAG, "Completed download.");
            // Reset the song list view
            final String path = Environment.getExternalStorageDirectory() + File.separator +
                    Environment.DIRECTORY_DOWNLOADS + File.separator + fileName;
            MediaScannerConnection.scanFile(context,
                    new String[] { path }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path2, Uri uri) {
                            ((MainActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
                                    ((MainActivity) context).updateNew(path, fileLink);
                                    // if (mp == null) {
                                        // TODO: Add error message!
                                        // Log.e(TAG, "Download failed.");
                                    // }
                                }
                            });
                        }
                    });
        }
    };
}
