package com.hemendra.comicreader.model.source.comics;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.FailureReason;

public interface OnComicsLoadedListener {

    void onComicsLoaded(Comics comics);
    void onFailedToLoadComics(FailureReason reason);

    void onComicDetailsLoaded(Comic comic);
    void onFailedToLoadComicDetails(FailureReason reason);

    void onPagesLoaded(Chapter chapter);
    void onFailedToLoadPages(FailureReason reason);

}
