package com.hemendra.comicreader.model.source.comics;

import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource.FailureReason;

public interface IComicsDataSourceListener {

    void onStartedLoadingComics();
    void onComicsLoaded(@NonNull Comics comics);
    void onFailedToLoadComics(@NonNull FailureReason reason);
    void onStoppedLoadingComics();

}
