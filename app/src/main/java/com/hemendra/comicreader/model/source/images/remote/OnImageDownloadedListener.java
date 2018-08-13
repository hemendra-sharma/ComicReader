package com.hemendra.comicreader.model.source.images.remote;

import android.graphics.Bitmap;

interface OnImageDownloadedListener {

    void onImageDownloaded(String url, Bitmap bmp, boolean image, boolean page);
    void onFailedToDownloadImage(String url, boolean image, boolean page);

}
