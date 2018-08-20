/*
 * Copyright (c) 2018 Hemendra Sharma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hemendra.comicreader.model.source.images.local;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Page;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.ImagesDataSource;
import com.hemendra.comicreader.model.utils.Utils;
import com.hemendra.comicreader.view.ImageAndViewHolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Provides the functionality to smoothly load the images from cache if available.
 */
public class LocalImagesDataSource extends ImagesDataSource implements OnImageLoadedListener {

    private static final int MAX_PARALLEL_LOADS = 10;

    private final ImagesDB db;
    private File chaptersDirectory;

    private LocalImageLoader[] loadingSlots = new LocalImageLoader[MAX_PARALLEL_LOADS];

    private int maxQueuedDownloads, cover_size_x, cover_size_y;
    private final ArrayList<LocalImageLoader> queuedDownloads = new ArrayList<>();

    /**
     * Creates a new instance of {@link LocalImagesDataSource}
     * @param context The Android application context
     * @param listener An instance of {@link IImagesDataSourceListener}, which in this case, is
     *                 {@link com.hemendra.comicreader.presenter.ComicsPresenter}
     */
    public LocalImagesDataSource(Context context, IImagesDataSourceListener listener) {
        super(context, listener);
        db = new ImagesDB(context).open();
        chaptersDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + getContext().getPackageName() + "/cache/chapters");
        maxQueuedDownloads = context.getResources().getInteger(R.integer.max_queued_image_loaders);
        cover_size_x = context.getResources().getDimensionPixelSize(R.dimen.cover_size_x);
        cover_size_y = context.getResources().getDimensionPixelSize(R.dimen.cover_size_y);
    }

    /**
     * Triggers a background thread to start loading the image from cache.
     * @param url The image URL
     * @param holder The view holder which will handle this image after getting it.
     */
    @Override
    public void loadImage(String url, ImageAndViewHolder holder) {
        if(listener != null) {
            if(!alreadyDownloading(url)) {
                if(!fitIntoAnyFreeSlot(url, holder)) {
                    if(!removeOldestAndAddNewDownload(url, holder)) {
                        listener.onFailedToLoadImage(FailureReason.UNKNOWN_LOCAL_ERROR, url, holder);
                    }
                }
            } else {
                listener.onFailedToLoadImage(FailureReason.ALREADY_LOADING, url, holder);
            }
        }
    }

    /**
     * Triggers a background thread to start loading the page from cache.
     * @param url The page URL
     * @param holder The view holder which will handle this image after getting it.
     */
    @Override
    public void loadPage(String url, ImageAndViewHolder holder) {
        if(listener != null) {
            Bitmap bmp = getPageFromCache(url);
            if(bmp != null) {
                holder.setImage(bmp);
            } else {
                listener.onFailedToLoadPage(FailureReason.NOT_AVAILABLE_LOCALLY, url, holder);
            }
        }
    }

    @Override
    public void onImageDownloaded(String url, Bitmap bmp, boolean image, boolean page) {
        popFromQueueAndFitIntoFreeSlot();
        nullifyInactiveSlots();
    }

    @Override
    public void onFailedToDownloadImage(String url, ImageAndViewHolder holder) {
        if(listener != null) {
            if (holder.isCover())
                listener.onFailedToLoadImage(FailureReason.NOT_AVAILABLE_LOCALLY, url, holder);
            else if(holder.isPage())
                listener.onFailedToLoadPage(FailureReason.NOT_AVAILABLE_LOCALLY, url, holder);
            popFromQueueAndFitIntoFreeSlot();
        }
        nullifyInactiveSlots();
    }

    /**
     * Returns an image Bitmap by reading the image data from database as byte-array.
     * @param url The image URL
     * @return Decoded Bitmap
     */
    public Bitmap getImageFromCache(String url) {
        Bitmap bmp = null;
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
        return bmp;
    }

    /**
     * Returns an image Bitmap by reading the image data from database as byte-array.
     * @param url The image URL
     * @return Decoded Bitmap
     */
    private Bitmap getPageFromCache(String url) {
        Bitmap bmp = null;
        byte[] bytes = db.getPage(url);
        if (bytes != null && bytes.length > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inSampleSize = 1;
            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        }
        return bmp;
    }

    /**
     * Save the image into image cache by inserting the blob data into database.
     * @param url The image URL
     * @param bmp The bitmap to be saved
     */
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

    /**
     * Save the page into image cache by inserting the blob data into database.
     * @param url The page URL
     * @param bmp The bitmap to be saved
     */
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

    /**
     * Checks whether the chapter was downloaded offline and is available or not.
     * @param chapter The chapter to check for.
     * @return TRUE if available offline, FALSE otherwise.
     */
    public boolean isChapterOffline(Chapter chapter) {
        if(listener != null) {
            File file = new File(chaptersDirectory, chapter.id+".obj");
            return file.exists() && file.length() > 0;
        }
        return false;
    }

    /**
     * Reads the offline chapter and returns as an instance of {@link Chapter}
     * @param chapter The chapter to read from local storage.
     * @return Instance of the read {@link Chapter} object.
     */
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

    private boolean fitIntoAnyFreeSlot(String url, ImageAndViewHolder holder) {
        return fitIntoAnyFreeSlot(new LocalImageLoader(this, url, holder))
                || queueDownload(url, holder);
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

    private boolean removeOldestAndAddNewDownload(String url, ImageAndViewHolder holder) {
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
                queueDownload(url, holder);
            } else {
                // replace the slot with new download
                loadingSlots[index] = new LocalImageLoader(this, url, holder);
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

    private boolean queueDownload(String url, ImageAndViewHolder holder) {
        synchronized (queuedDownloads) {
            if(queuedDownloads.size() < maxQueuedDownloads) {
                queuedDownloads.add(new LocalImageLoader(this, url, holder));
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

    /**
     * Stop ongoing processes, close the database connection, and release any memory so GC can collect it.
     */
    @Override
    public void dispose() {
        db.close();
        for(LocalImageLoader slot : loadingSlots) {
            if(slot != null)
                slot.cancel(true);
        }
        listener = null;
    }
}
