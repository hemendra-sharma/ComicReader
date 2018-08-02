package com.hemendra.comicreader.model.source.images.remote;

import android.graphics.Bitmap;

public interface OnImageDownloadedListener {

    public void onImageDownloaded(String url, Bitmap bmp, boolean image, boolean page);

}
