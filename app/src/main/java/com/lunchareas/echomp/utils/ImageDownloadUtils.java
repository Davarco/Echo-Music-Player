package com.lunchareas.echomp.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageDownloadUtils {

    private final static String TAG = ImageDownloadUtils.class.getName();

    public static void downloadSongArt(String url, long songId, long albumId, Context context) {
        Log.e(TAG, "Downloading song art.");
        // Picasso.with(context).load(url).into(picassoImageTarget(context, songId, albumId));

        // Use stream to load image
        int rand = (int)(Math.random()*1000000000);
        File imageFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), Integer.toString(rand));

        // Load image thru url in bitmap
        Bitmap bitmap = null;
        try {
            InputStream input = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save the image to disk
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Change the album art and finish
        // Log.e(TAG, Long.toString(id));
        // Log.e(TAG, imageFile.getPath());
        MediaDataUtils.changeAlbumArtWithSongId(imageFile.getPath(), songId, context);
        // Log.e(TAG, "Image saved to " + imageFile.getPath());
    }

    private static Target picassoImageTarget(final Context context, final long songId, final long albumId) {
        // String path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        // final File directory = cw.getDir(new File(path).getAbsolutePath(), Context.MODE_PRIVATE);
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // Use stream to load image
                        int rand = (int)(Math.random()*1000000);
                        final File imageFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), Integer.toString(rand));
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(imageFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (fos != null)
                                    fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        // TODO For some reason, album id changes by this point
                        // Uses song id to find newest album id
                        // Log.e(TAG, Long.toString(id));
                        // Log.e(TAG, imageFile.getPath());
                        MediaDataUtils.changeAlbumArtWithSongId(imageFile.getPath(), songId, context);
                        Log.d(TAG, "Image saved to " + imageFile.getPath());
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {}
            }
        };
    }
}
