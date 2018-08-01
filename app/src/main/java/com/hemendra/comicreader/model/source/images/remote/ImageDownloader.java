package com.hemendra.comicreader.model.source.images.remote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.RemoteConfig;
import com.hemendra.comicreader.model.source.comics.remote.ComicsParser;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;

import java.net.HttpURLConnection;

public class ImageDownloader extends CustomAsyncTask<Integer,Void,Bitmap> {

    private static Bitmap[] bitmapCache = new Bitmap[RemoteImagesDataSource.MAX_PARALLEL_DOWNLOADS];

    private OnImageDownloadedListener listener;
    public String imgUrl;
    private ImageView iv;
    public long startedAt = 0;
    private FailureReason reason = FailureReason.UNKNOWN_REMOTE_ERROR;

    public ImageDownloader(OnImageDownloadedListener listener,
                           String imgUrl, ImageView iv) {
        this.listener = listener;
        this.imgUrl = imgUrl;
        this.iv = iv;
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
                        public void onResponseCode(int code) {
                            switch (code) {
                                case HttpURLConnection.HTTP_NOT_FOUND:
                                    reason = FailureReason.API_MISSING;
                                    break;
                                default:
                                    reason = FailureReason.INVALID_RESPONSE_FROM_SERVER;
                            }
                        }
                    });
            if (bytes != null && bytes.length > 0) {
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
            iv.setImageBitmap(bitmap);
            if (listener != null)
                listener.onImageDownloaded(imgUrl, bitmap);
        }
    }
}
