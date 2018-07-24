package com.hemendra.comicreader.view.reader;

import android.graphics.Bitmap;

public interface IComicReaderCallback {

    void onPageDownloaded(String url, Bitmap pageImage);
    void onFailedToDownloadPage(String url, String reason);

}
