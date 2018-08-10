package com.hemendra.comicreader.model.source.images.remote;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.source.FailureReason;

public interface OnChapterDownloadListener {
    void onChapterDownloaded(Chapter chapter);
    void onProgressUpdate(Integer... progress);
    void onFailedToDownloadChapter(FailureReason reason);
}
