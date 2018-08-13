package com.hemendra.comicreader.model.source.images.remote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.reader.TouchImageView;

import java.net.HttpURLConnection;

public class ImageDownloader extends CustomAsyncTask<Integer,Void,Bitmap> {

    private static Bitmap[] bitmapCache = new Bitmap[RemoteImagesDataSource.MAX_PARALLEL_DOWNLOADS];

    private OnImageDownloadedListener listener;
    public String imgUrl;
    private ImageView iv;
    private TouchImageView tiv;
    public long startedAt = 0;
    private HttpURLConnection connection = null;

    public ImageDownloader(OnImageDownloadedListener listener,
                           String imgUrl, ImageView iv, TouchImageView tiv) {
        this.listener = listener;
        this.imgUrl = imgUrl;
        this.iv = iv;
        this.tiv = tiv;
    }

    @Override
    public void cancel(boolean interrupt) {
        try{
            if(connection != null)
                connection.disconnect();
        }catch (Throwable ex) {
            ex.printStackTrace();
        }
        super.cancel(interrupt);
    }

    @Override
    protected void onPreExecute() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        try {
            int inBitmapIndex = params[0];
            byte[] bytes = ContentDownloader.downloadAsByteArray(imgUrl,
                    new ConnectionCallback() {
                        @Override
                        public void onConnectionInitialized(HttpURLConnection conn) {
                            connection = conn;
                        }
                    });
            if (!isCancelled() && bytes != null && bytes.length > 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                options.inSampleSize = 1;
                if(bitmapCache[inBitmapIndex] != null) {
                    options.inBitmap = bitmapCache[inBitmapIndex];
                }
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                if(bitmapCache[inBitmapIndex] == null) {
                    bitmapCache[inBitmapIndex] = bmp;
                }
                return bmp;
            }
        }catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(bitmap != null) {
            if(iv != null)
                iv.setImageBitmap(bitmap);
            else if(tiv != null) {
                tiv.setImageBitmap(bitmap);
                tiv.setTag(-1);
            }
            //
            if (listener != null)
                listener.onImageDownloaded(imgUrl, bitmap, iv != null, tiv != null);
        } else if (listener != null)
            listener.onFailedToDownloadImage(imgUrl, iv != null, tiv != null);
    }
}
