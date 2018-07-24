package com.hemendra.comicreader.view.details;

import com.hemendra.comicreader.model.data.Comic;

public interface IComicDetailsCallback {

    void onComicDetailsLoadingStarted();
    void onComicDetailsLoaded(Comic comic);
    void onFailedToLoadComicDetails(String reason);
    void onStoppedLoadingComicDetails();

}
