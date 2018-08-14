package com.hemendra.comicreader.view;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;

public interface IComicListActivityCallback {

    void askForPermissions();

    void onComicDetailsLoadingStarted();
    void onComicDetailsLoaded(Comic comic);
    void onComicUpdated(Comic comic);
    void onFailedToLoadComicDetails(String reason);

    void onComicsLoadingStarted();
    void onComicsLoaded(Comics comics);
    void onFailedToLoadComics(String reason);

    void onChapterLoadingStarted();
    void onChapterLoaded(Chapter chapter);
    void onFailedToLoadChapter(String reason);

    void onPageLoaded();

    Chapter getNextChapterFromDetailsFragment(Chapter ch);

    void showProgress();
    void hideProgress();

}
