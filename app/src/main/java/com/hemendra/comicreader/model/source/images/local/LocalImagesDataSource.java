package com.hemendra.comicreader.model.source.images.local;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.ImagesDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LocalImagesDataSource extends ImagesDataSource {

    private ImagesDB db;

    public LocalImagesDataSource(Context context, IImagesDataSourceListener listener) {
        super(context, listener);
        db = new ImagesDB(context).open();
    }

    @Override
    public void loadImage(String url, ImageView iv) {
        if(listener != null) {
            Bitmap bmp = getImageFromCache(url);
            if (bmp != null) {
                iv.setImageBitmap(bmp);
            } else {
                listener.onFailedToLoadImage(FailureReason.NOT_AVAILABLE_LOCALLY, url, iv);
            }
        }
    }

    @Override
    public void stopLoadingImage(String url) {

    }

    private Bitmap getImageFromCache(String url) {
        Bitmap bmp = null;
        try{
            byte[] bytes = db.getImage(url);
            if (bytes != null && bytes.length > 0) {
                // we already have the image. resize and return...
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                options.inSampleSize = 1;
                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            }
        }catch (Throwable ex) {
            ex.printStackTrace();
        }
        return bmp;
    }

    public void saveImage(String url, Bitmap bmp) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            db.insertImage(url, out.toByteArray());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        db.close();
        listener = null;
    }
}
