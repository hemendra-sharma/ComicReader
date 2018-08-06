package com.hemendra.comicreader.model.source.images.remote;

import android.widget.ImageView;

import com.hemendra.comicreader.model.data.Chapter;

public interface OnChapterDownloadListener {
    void onChapterDownloaded(Chapter chapter, ImageView iv);
    void onAlreadyLoading(Chapter chapter, ImageView iv);
    void onFailedToDownloadChapter(Chapter chapter, ImageView iv);
}
