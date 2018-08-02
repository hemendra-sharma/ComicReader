package com.hemendra.comicreader.model.source.images.remote;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.ImagesDataSource;
import com.hemendra.comicreader.view.reader.TouchImageView;

public class RemoteImagesDataSource extends ImagesDataSource implements OnImageDownloadedListener {

    public static final int MAX_PARALLEL_DOWNLOADS = 10;

    private IImagesDataSourceListener listener;

    private ImageDownloader[] downloadingSlots = new ImageDownloader[MAX_PARALLEL_DOWNLOADS];

    public RemoteImagesDataSource(Context context, IImagesDataSourceListener listener) {
        super(context, listener);
        this.listener = listener;
    }

    @Override
    public void loadImage(String url, ImageView iv) {
        if(listener != null) {
            if(!alreadyDownloading(url)) {
                if(!fitIntoAnyFreeSlot(url, iv, null)) {
                    if(!removeOldestAndAddNewDownload(url, iv, null)) {
                        listener.onFailedToLoadImage(FailureReason.UNKNOWN_REMOTE_ERROR, url, iv);
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
            if(!alreadyDownloading(url)) {
                if(!fitIntoAnyFreeSlot(url, null, iv)) {
                    if(!removeOldestAndAddNewDownload(url, null, iv)) {
                        listener.onFailedToLoadPage(FailureReason.UNKNOWN_REMOTE_ERROR, url, iv);
                    }
                }
            } else {
                listener.onFailedToLoadPage(FailureReason.ALREADY_LOADING, url, iv);
            }
        }
    }

    @Override
    public void onImageDownloaded(String url, Bitmap bmp, boolean image, boolean page) {
        if(listener != null) {
            listener.onImageLoaded(url, bmp);
            if(page)
                listener.onPageLoaded();
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
        return false;
    }

    private boolean fitIntoAnyFreeSlot(String url, ImageView iv, TouchImageView tiv) {
        for(int i = 0; i< downloadingSlots.length; i++) {
            if(downloadingSlots[i] == null || !downloadingSlots[i].isExecuting()) {
                downloadingSlots[i] = new ImageDownloader(this, url, iv, tiv);
                downloadingSlots[i].execute(i);
                return true;
            }
        }
        return false;
    }

    private boolean removeOldestAndAddNewDownload(String url, ImageView iv, TouchImageView tiv) {
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
            // replace the slot with new download
            downloadingSlots[index] = new ImageDownloader(this, url, iv, tiv);
            downloadingSlots[index].execute(index);
            return true;
        }
        return false;
    }

    @Override
    public void stopLoadingImage(String url) {
        for(ImageDownloader slot : downloadingSlots) {
            if(slot != null && slot.isExecuting()) {
                slot.cancel(true);
            }
        }
    }

    @Override
    public void dispose() {
        listener = null;
    }
}
