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

package com.hemendra.comicreader.model.source.images.remote;

import android.content.Context;
import android.graphics.Bitmap;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.ImagesDataSource;
import com.hemendra.comicreader.view.ImageAndViewHolder;

import java.util.ArrayList;

/**
 * Provides the functionality to download the images from remote server.
 */
public class RemoteImagesDataSource extends ImagesDataSource implements OnImageDownloadedListener {

    private static final int MAX_PARALLEL_DOWNLOADS = 10;

    private IImagesDataSourceListener listener;
    private ChapterPagesDownloader chapterPagesDownloader = null;

    private ImageDownloader[] downloadingSlots = new ImageDownloader[MAX_PARALLEL_DOWNLOADS];

    private int maxQueuedDownloads;
    private final ArrayList<ImageDownloader> queuedDownloads = new ArrayList<>();

    /**
     * Creates a new instance of {@link RemoteImagesDataSource}
     * @param context The Android application context
     * @param listener An instance of {@link IImagesDataSourceListener}, which in this case, is
     *                 {@link com.hemendra.comicreader.presenter.ComicsPresenter}
     */
    public RemoteImagesDataSource(Context context, IImagesDataSourceListener listener) {
        super(context, listener);
        this.listener = listener;
        maxQueuedDownloads = context.getResources().getInteger(R.integer.max_queued_image_loaders);
    }

    /**
     * Triggers a background thread to start downloading the image from server.
     * @param url The image URL
     * @param holder The view holder which will handle this image after getting it.
     */
    @Override
    public void loadImage(String url, ImageAndViewHolder holder) {
        if(listener != null) {
            if(!alreadyDownloading(url)) {
                if(!fitIntoAnyFreeSlot(url, holder)) {
                    if(!removeOldestAndAddNewDownload(url, holder)) {
                        listener.onFailedToLoadImage(FailureReason.UNKNOWN_REMOTE_ERROR, url, holder);
                    }
                }
            } else {
                listener.onFailedToLoadImage(FailureReason.ALREADY_LOADING, url, holder);
            }
        }
    }

    /**
     * Triggers a background thread to start downloading the page from server.
     * @param url The page URL
     * @param holder The view holder which will handle this image after getting it.
     */
    @Override
    public void loadPage(String url, ImageAndViewHolder holder) {
        if(listener != null) {
            if(!alreadyDownloading(url)) {
                if(!fitIntoAnyFreeSlot(url, holder)) {
                    if(!removeOldestAndAddNewDownload(url, holder)) {
                        listener.onFailedToLoadPage(FailureReason.UNKNOWN_REMOTE_ERROR, url, holder);
                    }
                }
            } else {
                listener.onFailedToLoadPage(FailureReason.ALREADY_LOADING, url, holder);
            }
        }
    }

    /**
     * Triggers a background thread to start downloading all the pages of a chapter, from server.
     * @param chapter The chapter whose pages needs to be downloaded.
     * @param listener The callback listener.
     */
    public void downloadChapter(Chapter chapter, OnChapterDownloadListener listener) {
        if(listener != null) {
            if (chapterPagesDownloader == null
                    || !chapterPagesDownloader.isExecuting()) {
                chapterPagesDownloader = new ChapterPagesDownloader(getContext(), listener, chapter);
                chapterPagesDownloader.execute();
            } else {
                listener.onFailedToDownloadChapter(FailureReason.ALREADY_LOADING);
            }
        }
    }

    /**
     * Aborts the current ongoing chapter download.
     */
    public void stopDownloadingChapter() {
        if(chapterPagesDownloader != null && chapterPagesDownloader.isExecuting()) {
            chapterPagesDownloader.cancel(true);
        }
    }

    @Override
    public void onImageDownloaded(String url, Bitmap bmp, boolean image, boolean page) {
        if(listener != null) {
            if(image)
                listener.onImageLoaded(url, bmp);
            if(page)
                listener.onPageLoaded(url, bmp);
        }
        popFromQueueAndFitIntoFreeSlot();
    }

    @Override
    public void onFailedToDownloadImage(String url, boolean image, boolean page) {
        popFromQueueAndFitIntoFreeSlot();
    }

    private void popFromQueueAndFitIntoFreeSlot() {
        ImageDownloader id = getElementFromQueue();
        if(id != null) {
            fitIntoAnyFreeSlot(id);
        }
    }

    private boolean alreadyDownloading(String url) {
        for (ImageDownloader slot : downloadingSlots) {
            if (slot != null && slot.isExecuting()
                    && slot.imgUrl.equals(url)) {
                // already downloading
                return true;
            }
        }
        synchronized (queuedDownloads) {
            for (ImageDownloader slot : queuedDownloads) {
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
        return fitIntoAnyFreeSlot(new ImageDownloader(this, url, holder))
                || queueDownload(url, holder);
    }

    private boolean fitIntoAnyFreeSlot(ImageDownloader id) {
        for(int i = 0; i< downloadingSlots.length; i++) {
            if(downloadingSlots[i] == null || !downloadingSlots[i].isExecuting()) {
                downloadingSlots[i] = id;
                downloadingSlots[i].execute();
                return true;
            }
        }
        return false;
    }

    private boolean removeOldestAndAddNewDownload(String url, ImageAndViewHolder holder) {
        int index = -1;
        long oldestTimestamp = System.currentTimeMillis();
        for(int i = 0; i< downloadingSlots.length; i++) {
            if(downloadingSlots[i] == null || downloadingSlots[i].startedAt < oldestTimestamp) {
                oldestTimestamp = downloadingSlots[i].startedAt;
                index = i;
            }
        }
        if(index >= 0) {
            // found index...
            if(downloadingSlots[index] != null && downloadingSlots[index].isExecuting()) {
                // stop ongoing download
                downloadingSlots[index].cancel(true);
            }
            ImageDownloader id = getElementFromQueue();
            if(id != null) {
                // pooped first item from queue... there was already queue downloads.
                downloadingSlots[index] = id;
                queueDownload(url, holder);
            } else {
                // replace the slot with new download
                downloadingSlots[index] = new ImageDownloader(this, url, holder);
            }
            downloadingSlots[index].execute();
            return true;
        }
        //
        return false;
    }

    private ImageDownloader getElementFromQueue() {
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
                queuedDownloads.add(new ImageDownloader(this, url, holder));
                return true;
            }
            return false;
        }
    }

    /**
     * Abort the ongoing image download process if there is any.
     * @param url The URL to search for in the ongoing processes.
     */
    @Override
    public void stopLoadingImage(String url) {
        boolean aborted = false;
        for(ImageDownloader slot : downloadingSlots) {
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
     * Stop ongoing processes, and release any memory so GC can collect it.
     */
    @Override
    public void dispose() {
        for(ImageDownloader slot : downloadingSlots) {
            if(slot != null)
                slot.cancel(true);
        }
        listener = null;
    }
}
