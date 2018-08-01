package com.hemendra.comicreader.model.source.comics;

import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource.SourceType;

public interface IComicsDataSourceListener {

    void onStartedLoadingComics();
    void onComicsLoaded(@NonNull Comics comics, @NonNull SourceType sourceType);
    void onFailedToLoadComics(@NonNull FailureReason reason);

    void onStartedLoadingComicDetails();
    void onComicDetailsLoaded(Comic comic);
    void onFailedToLoadComicDetails(FailureReason reason);

}
