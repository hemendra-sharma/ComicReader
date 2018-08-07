package com.hemendra.comicreader.model.source.images.local;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Page;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.ImagesDataSource;
import com.hemendra.comicreader.model.utils.Utils;
import com.hemendra.comicreader.view.reader.TouchImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class LocalImagesDataSource extends ImagesDataSource {

    private ImagesDB db;
    private File chaptersDirectory;

    public LocalImagesDataSource(Context context, IImagesDataSourceListener listener) {
        super(context, listener);
        db = new ImagesDB(context).open();
        chaptersDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + getContext().getPackageName() + "/cache/chapters");
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

    public boolean isChapterOffline(Chapter chapter) {
        if(listener != null) {
            File file = new File(chaptersDirectory, chapter.id+".obj");
            return file.exists() && file.length() > 0;
        }
        return false;
    }

    public Chapter getOfflineChapter(Chapter chapter) {
        if(listener != null && chapter.pages.size() > 0) {
            File file = new File(chaptersDirectory, chapter.id+".obj");
            chapter = (Chapter) Utils.readObjectFromFile(file);
            if(chapter != null) {
                boolean allPagesAvailable = true;
                for (Page page : chapter.pages) {
                    if (page.rawImageData == null
                            || page.rawImageData.length == 0) {
                        allPagesAvailable = false;
                    }
                }
                if(allPagesAvailable)
                    return chapter;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        db.close();
        listener = null;
    }
}
