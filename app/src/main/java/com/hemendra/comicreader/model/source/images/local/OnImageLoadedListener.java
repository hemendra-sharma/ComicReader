package com.hemendra.comicreader.model.source.images.local;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.hemendra.comicreader.view.reader.TouchImageView;

interface OnImageLoadedListener {

    void onImageDownloaded(String url, Bitmap bmp, boolean image, boolean page);
    void onFailedToDownloadImage(String url, ImageView iv, TouchImageView tiv);

}
