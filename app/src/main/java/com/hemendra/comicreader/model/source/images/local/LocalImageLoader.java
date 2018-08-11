package com.hemendra.comicreader.model.source.images.local;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.reader.TouchImageView;

public class LocalImageLoader extends CustomAsyncTask<Integer,Void,Bitmap> {

    private static Bitmap[] bitmapCache = new Bitmap[LocalImagesDataSource.MAX_PARALLEL_LOADS];

    private LocalImagesDataSource dataSource;
    public String imgUrl;
    private ImageView iv;
    private TouchImageView tiv;
    public long startedAt = 0;

    public LocalImageLoader(@NonNull LocalImagesDataSource dataSource,
                            String imgUrl, ImageView iv, TouchImageView tiv) {
        this.dataSource = dataSource;
        this.imgUrl = imgUrl;
        this.iv = iv;
        this.tiv = tiv;
    }

    @Override
    protected void onPreExecute() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        try {
            int inBitmapIndex = params[0];
            Bitmap bmp = null;
            if(iv != null) {
                bmp = dataSource.getImageFromCache(imgUrl, bitmapCache[inBitmapIndex]);
            } else if(tiv != null) {
                bmp = dataSource.getPageFromCache(imgUrl, bitmapCache[inBitmapIndex]);
            }
            if(bitmapCache[inBitmapIndex] == null && bmp != null) {
                bitmapCache[inBitmapIndex] = bmp;
            }
            return bmp;
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
            dataSource.onImageDownloaded(imgUrl, bitmap, iv != null, tiv != null);
        } else
            dataSource.onFailedToDownloadImage(imgUrl, iv, tiv);
    }
}
