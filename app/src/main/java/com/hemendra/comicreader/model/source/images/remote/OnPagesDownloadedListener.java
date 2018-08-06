package com.hemendra.comicreader.model.source.images.remote;

import android.widget.ImageView;

import com.hemendra.comicreader.model.data.Chapter;

public interface OnPagesDownloadedListener {
    void onPagesDownloaded(Chapter chapter, ImageView iv);
    void onAlreadyLoading(Chapter chapter, ImageView iv);
    void onFailedToDownloadPages(Chapter chapter, ImageView iv);
}
