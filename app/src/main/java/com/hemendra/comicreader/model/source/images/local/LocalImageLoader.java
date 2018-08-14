package com.hemendra.comicreader.model.source.images.local;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.hemendra.comicreader.model.utils.CustomAsyncTask;

public class LocalImageLoader extends CustomAsyncTask<Void,Void,Bitmap> {

    private LocalImagesDataSource dataSource;
    public String imgUrl;
    private ImageView iv;
    public long startedAt = 0;

    public LocalImageLoader(@NonNull LocalImagesDataSource dataSource,
                            String imgUrl, ImageView iv) {
        this.dataSource = dataSource;
        this.imgUrl = imgUrl;
        this.iv = iv;
    }

    @Override
    protected void onPreExecute() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            return dataSource.getImageFromCache(imgUrl);
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
            dataSource.onImageDownloaded(imgUrl, bitmap, iv != null, false);
        } else
            dataSource.onFailedToDownloadImage(imgUrl, iv, null);
    }
}
