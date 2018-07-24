package com.hemendra.comicreader.model.source;

import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.data.Comics;

public interface IComicsDataSourceListener {

    void onStartedLoadingComics();
    void onComicsLoaded(@NonNull Comics comics);
    void onFailedToLoadComics(String reason);
    void onStoppedLoadingComics();

}
