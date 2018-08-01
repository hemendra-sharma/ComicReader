package com.hemendra.comicreader.view;

import android.graphics.Bitmap;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;

import java.util.ArrayList;

public interface IComicListActivityCallback {

    void askForPermissions();

    void onComicDetailsLoadingStarted();
    void onComicDetailsLoaded(Comic comic);
    void onFailedToLoadComicDetails(String reason);

    void onComicsLoadingStarted();
    void onComicsLoaded(Comics comics);
    void onFailedToLoadComics(String reason);

    void onPageDownloaded(String url, Bitmap pageImage);
    void onFailedToDownloadPage(String url, String reason);

}
