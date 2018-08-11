package com.hemendra.comicreader.model.source.comics.local;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.Utils;
import com.hemendra.comicreader.view.list.SortingOption;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class LocalComicsDataSource extends ComicsDataSource implements OnComicsLoadedListener {

    public File comicsCacheFile;
    private LocalComicsLoader loader;
    private ComicsSearcher searcher = null;
    private ComicsSorter sorter = null;
    private ComicsFilterer filterer = null;
    private Comics comics = null;
    private SortingOption sortingOption = SortingOption.POPULARITY;
    private ArrayList<String> selectedCategories = new ArrayList<>();

    public LocalComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context, listener);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + getContext().getPackageName() + "/cache");
        comicsCacheFile = new File(dir, "comics.obj");
        loader = new LocalComicsLoader(this);
    }

    private boolean hasComics() {
        return comicsCacheFile.exists() && comicsCacheFile.length() > 0;
    }
    
    @Override
    public void loadComics() {
        if (hasComics()) {
            if(comics != null) {
                listener.onComicsLoaded(comics, SourceType.LOCAL_FULL);
            } else if(loader == null) {
                listener.onFailedToLoadComics(FailureReason.SOURCE_CLOSED);
            } else if (isNotAlreadyLoading()) {
                loader.execute(comicsCacheFile);
                listener.onStartedLoadingComics();
            } else {
                listener.onFailedToLoadComics(FailureReason.ALREADY_LOADING);
            }
        } else {
            listener.onFailedToLoadComics(FailureReason.NOT_AVAILABLE_LOCALLY);
        }
    }

    public void searchComics(String query) {
        if(comics != null) {
            if(isNotAlreadyLoading()) {
                searcher = new ComicsSearcher(comics, this, selectedCategories, sortingOption);
                searcher.execute(query);
                listener.onStartedLoadingComics();
            } else {
                listener.onFailedToLoadComics(FailureReason.ALREADY_LOADING);
            }
        } else {
            listener.onFailedToLoadComics(FailureReason.UNKNOWN_LOCAL_ERROR);
        }
    }

    public void sortComics(Comics comics, SortingOption option) {
        sortingOption = option;
        if(comics != null) {
            if(isNotAlreadyLoading()) {
                sorter = new ComicsSorter(this, option);
                sorter.execute(comics);
                listener.onStartedLoadingComics();
            } else {
                listener.onFailedToLoadComics(FailureReason.ALREADY_LOADING);
            }
        } else {
            listener.onFailedToLoadComics(FailureReason.UNKNOWN_LOCAL_ERROR);
        }
    }

    public void filterComics(ArrayList<String> selectedCategories) {
        this.selectedCategories = selectedCategories;
        if(comics != null) {
            if(isNotAlreadyLoading()) {
                filterer = new ComicsFilterer(comics, this, selectedCategories, sortingOption);
                filterer.execute();
                listener.onStartedLoadingComics();
            } else {
                listener.onFailedToLoadComics(FailureReason.ALREADY_LOADING);
            }
        } else {
            listener.onFailedToLoadComics(FailureReason.UNKNOWN_LOCAL_ERROR);
        }
    }

    private boolean isNotAlreadyLoading() {
        return (loader == null || !loader.isExecuting())
                && (searcher == null || !searcher.isExecuting())
                && (sorter == null || !sorter.isExecuting())
                && (filterer == null || !filterer.isExecuting());
    }

    @Override
    public void onComicsLoaded(Comics comics, SourceType sourceType) {
        if (comics != null) {
            if(sourceType == SourceType.LOCAL_FULL) {
                this.comics = comics;
                this.sortingOption = SortingOption.POPULARITY;
                this.selectedCategories = this.comics.categories;
            }
            listener.onComicsLoaded(comics, sourceType);
        } else
            listener.onFailedToLoadComics(FailureReason.UNKNOWN_LOCAL_ERROR);
    }

    @Override
    public void onFailedToLoadComics(FailureReason reason) {
        listener.onFailedToLoadComics(reason);
    }

    @Override
    public void onComicDetailsLoaded(Comic comic) {

    }

    @Override
    public void onFailedToLoadComicDetails(FailureReason reason) {

    }

    @Override
    public void onPagesLoaded(Chapter chapter) {

    }

    @Override
    public void onFailedToLoadPages(FailureReason reason) {

    }

    @Override
    protected void stopLoadingComics() {
        if (loader != null && loader.isExecuting())
            loader.cancel(true);
        if (searcher != null && searcher.isExecuting())
            searcher.cancel(true);
        if (sorter != null && sorter.isExecuting())
            sorter.cancel(true);
        if (filterer != null && filterer.isExecuting())
            filterer.cancel(true);
    }

    public void deleteCache() {
        Utils.deleteFile(comicsCacheFile);
        comics = null;
    }

    public void save(@NonNull Comics comics) {
        this.comics = comics;
        new Thread(() -> Utils.writeToFile(this.comics, comicsCacheFile)).start();
    }

    public void updateComic(@NonNull Comic comic) {
        boolean updated = false;
        for(int i=0; i<comics.comics.size(); i++) {
            if(comics.comics.get(i).id.equals(comic.id)) {
                comics.comics.set(i, comic);
                updated = true;
                break;
            }
        }
        if(updated)
            save(comics);
    }

    public void updateChapter(@NonNull Chapter chapter) {
        boolean updated = false;
        for(Comic comic : comics.comics) {
            for(int i=0; i<comic.chapters.size(); i++) {
                if(comic.chapters.get(i).id.equals(chapter.id)) {
                    comic.chapters.set(i, chapter);
                    updated = true;
                    break;
                }
            }
        }
        if(updated)
            save(comics);
    }

    @Override
    public void dispose() {
        stopLoadingComics();
        loader = null;
        listener = null;
        comics = null;
    }
}
