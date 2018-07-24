package com.hemendra.comicreader.presenter;

import com.hemendra.comicreader.model.data.Comics;

import java.util.ArrayList;

public interface ComicListActivityView {

    void onComicsLoaded(Comics comics);
    void onFailedToLoadComics(String reason);

}
