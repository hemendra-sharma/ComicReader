package com.hemendra.comicreader.model.source.images.local;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import com.hemendra.comicreader.R;
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
import java.util.ArrayList;

public class LocalImagesDataSource extends ImagesDataSource implements OnImageLoadedListener {

    private static final int MAX_PARALLEL_LOADS = 10;

    private final ImagesDB db;
    private File chaptersDirectory;

    private LocalImageLoader[] loadingSlots = new LocalImageLoader[MAX_PARALLEL_LOADS];

    private int maxQueuedDownloads, cover_size_x, cover_size_y;
    private final ArrayList<LocalImageLoader> queuedDownloads = new ArrayList<>();

    public LocalImagesDataSource(Context context, IImagesDataSourceListener listener) {
        super(context, listener);
        db = new ImagesDB(context).open();
        chaptersDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + getContext().getPackageName() + "/cache/chapters");
        maxQueuedDownloads = context.getResources().getInteger(R.integer.max_queued_image_loaders);
        cover_size_x = context.getResources().getDimensionPixelSize(R.dimen.cover_size_x);
        cover_size_y = context.getResources().getDimensionPixelSize(R.dimen.cover_size_y);
    }

    @Override
    public void loadImage(String url, ImageView iv) {
        if(listener != null) {
            if(!alreadyDownloading(url)) {
                if(!fitIntoAnyFreeSlot(url, iv)) {
                    if(!removeOldestAndAddNewDownload(url, iv)) {
                        listener.onFailedToLoadImage(FailureReason.UNKNOWN_LOCAL_ERROR, url, iv);
                    }
                }
            } else {
                listener.onFailedToLoadImage(FailureReason.ALREADY_LOADING, url, iv);
            }
        }
    }

    @Override
    public void loadPage(String url, TouchImageView iv) {
        if(listener != null) {
            Bitmap bmp = getPageFromCache(url);
            if(bmp != null) {
                iv.setImageBitmap(bmp);
                iv.setTag(-1);
            } else {
                listener.onFailedToLoadPage(FailureReason.NOT_AVAILABLE_LOCALLY, url, iv);
            }
        }
    }

    @Override
    public void onImageDownloaded(String url, Bitmap bmp, boolean image, boolean page) {
        popFromQueueAndFitIntoFreeSlot();
        nullifyInactiveSlots();
    }

    @Override
    public void onFailedToDownloadImage(String url, ImageView iv, TouchImageView tiv) {
        if(listener != null) {
            if (iv != null)
                listener.onFailedToLoadImage(FailureReason.NOT_AVAILABLE_LOCALLY, url, iv);
            else if (tiv != null)
                listener.onFailedToLoadPage(FailureReason.NOT_AVAILABLE_LOCALLY, url, tiv);
            popFromQueueAndFitIntoFreeSlot();
        }
        nullifyInactiveSlots();
    }

    public Bitmap getImageFromCache(String url) {
        Bitmap bmp = null;
        try{
            byte[] bytes = db.getImage(url);
            if (bytes != null && bytes.length > 0) {
                // we already have the image. resize and return...
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                options.inSampleSize = 1;

                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

                options.inSampleSize = Utils.calculateInSampleSize(options,
                        cover_size_x, cover_size_y);

                options.inJustDecodeBounds = false;

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

    private void nullifyInactiveSlots() {
        for(int i=0; i<loadingSlots.length; i++) {
            if(loadingSlots[i] != null && !loadingSlots[i].isExecuting()) {
                loadingSlots[i] = null;
            }
        }
    }

    private void popFromQueueAndFitIntoFreeSlot() {
        LocalImageLoader id = getElementFromQueue();
        if(id != null) fitIntoAnyFreeSlot(id);
    }

    private boolean alreadyDownloading(String url) {
        for (LocalImageLoader slot : loadingSlots) {
            if (slot != null && slot.isExecuting()
                    && slot.imgUrl.equals(url)) {
                // already downloading
                return true;
            }
        }
        synchronized (queuedDownloads) {
            for (LocalImageLoader slot : queuedDownloads) {
                if (slot != null && slot.isExecuting()
                        && slot.imgUrl.equals(url)) {
                    // already queued
                    return true;
                }
            }
        }
        return false;
    }

    private boolean fitIntoAnyFreeSlot(String url, ImageView iv) {
        return fitIntoAnyFreeSlot(new LocalImageLoader(this, url, iv))
                || queueDownload(url, iv);
    }

    private boolean fitIntoAnyFreeSlot(LocalImageLoader id) {
        for(int i = 0; i< loadingSlots.length; i++) {
            if(loadingSlots[i] == null || !loadingSlots[i].isExecuting()) {
                loadingSlots[i] = id;
                loadingSlots[i].execute();
                return true;
            }
        }
        return false;
    }

    private boolean removeOldestAndAddNewDownload(String url, ImageView iv) {
        int index = -1;
        long oldestTimestamp = System.currentTimeMillis();
        for(int i = 0; i< loadingSlots.length; i++) {
            if(loadingSlots[i] == null || loadingSlots[i].startedAt < oldestTimestamp) {
                oldestTimestamp = loadingSlots[i].startedAt;
                index = i;
            }
        }
        if(index >= 0) {
            // found index...
            if(loadingSlots[index] != null && loadingSlots[index].isExecuting()) {
                // stop ongoing download
                loadingSlots[index].cancel(true);
            }
            LocalImageLoader id = getElementFromQueue();
            if(id != null) {
                // pooped first item from queue... there was already queue downloads.
                loadingSlots[index] = id;
                queueDownload(url, iv);
            } else {
                // replace the slot with new download
                loadingSlots[index] = new LocalImageLoader(this, url, iv);
            }
            loadingSlots[index].execute();
            return true;
        }
        //
        return false;
    }

    private LocalImageLoader getElementFromQueue() {
        synchronized (queuedDownloads) {
            if(queuedDownloads.size() > 0)
                return queuedDownloads.remove(0);
            else
                return null;
        }
    }

    private boolean queueDownload(String url, ImageView iv) {
        synchronized (queuedDownloads) {
            if(queuedDownloads.size() < maxQueuedDownloads) {
                queuedDownloads.add(new LocalImageLoader(this, url, iv));
                return true;
            }
            return false;
        }
    }

    @Override
    public void stopLoadingImage(String url) {
        boolean aborted = false;
        for(LocalImageLoader slot : loadingSlots) {
            if(slot != null && slot.imgUrl.equals(url)) {
                slot.cancel(true);
                aborted = true;
            }
        }
        if(aborted) {
            popFromQueueAndFitIntoFreeSlot();
        }
    }

    @Override
    public void dispose() {
        db.close();
        listener = null;
    }
}
