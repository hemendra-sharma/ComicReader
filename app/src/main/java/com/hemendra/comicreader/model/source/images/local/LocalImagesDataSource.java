package com.hemendra.comicreader.model.source.images.local;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Page;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.ImagesDataSource;
import com.hemendra.comicreader.view.reader.TouchImageView;

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
    public void loadPage(String url, TouchImageView iv) {
        if(listener != null) {
            Bitmap bmp = getPageFromCache(url);
            if (bmp != null) {
                iv.setImageBitmap(bmp);
                iv.setTag(-1);
                listener.onPageLoaded(null, null);
            } else {
                listener.onFailedToLoadPage(FailureReason.NOT_AVAILABLE_LOCALLY, url, iv);
            }
        }
    }

    public boolean isChapterAvailableOffline(Chapter chapter) {
        if(listener != null && chapter.pages.size() > 0) {
            for(Page page : chapter.pages) {
                if(!db.hasPage(page.getImageUrl())) {
                    return false;
                }
            }
            return true;
        }
        return false;
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

    private Bitmap getPageFromCache(String url) {
        Bitmap bmp = null;
        try{
            byte[] bytes = db.getPage(url);
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

    public void savePage(String url, Bitmap bmp) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            db.insertPage(url, out.toByteArray());
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
