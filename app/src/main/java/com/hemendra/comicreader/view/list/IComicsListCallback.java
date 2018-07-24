package com.hemendra.comicreader.view.list;

import com.hemendra.comicreader.model.data.Comics;

public interface IComicsListCallback {

    void onComicsLoadingStarted();
    void onComicsLoaded(Comics comics);
    void onFailedToLoadComics(String reason);
    void onStoppedLoadingComics();

}
