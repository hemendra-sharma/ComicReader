/*
 * Copyright (c) 2018 Hemendra Sharma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Handles the operations which are related to reading / writing comics data to local storage.
 * @author Hemendra Sharma
 */
public class LocalComicsDataSource extends ComicsDataSource implements OnComicsLoadedListener {

    public File comicsCacheFile;
    private LocalComicsLoader loader;
    private ComicsSearcher searcher = null;
    private ComicsSorter sorter = null;
    private ComicsFilterer filterer = null;
    private Comics comics = null;
    private SortingOption sortingOption = SortingOption.POPULARITY;
    private ArrayList<String> selectedCategories = new ArrayList<>();

    /**
     * Creates a new instance of {@link LocalComicsDataSource}
     * @param context The android application context
     * @param listener An instance of {@link IComicsDataSourceListener} which in this case,
     *                 is {@link com.hemendra.comicreader.presenter.ComicsPresenter}
     */
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

    /**
     * Triggers the background thread which loads comics list from local internal storage asynchronously.
     */
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

    /**
     * Triggers the background thread which performs a search on the already-loaded comics list asynchronously.
     * @param query The search keyword entered by user.
     */
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

    /**
     * Triggers a background thread which performs the sorting operation on the given list of
     * comics. The sorting operation is performed asynchronously.
     * @param comics The data-set on which sorting is supposed to be performed.
     * @param option What type of sorting needs to be performed. It can be one of the:
     *               {@link SortingOption}.POPULARITY,
     *               {@link SortingOption}.LATEST_FIRST,
     *               {@link SortingOption}.A_TO_Z
     */
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

    /**
     * Triggers a background thread which filters the full data-set of comics by the
     * given list of categories.
     * @param selectedCategories List of categories.
     */
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

    /**
     * Delete the local copy of the comics data-set.
     */
    public void deleteCache() {
        Utils.deleteFile(comicsCacheFile);
        comics = null;
    }

    /**
     * Save the comics data-set as a cache file.
     * @param comics The data-set that needs to be cached.
     */
    public void save(@NonNull Comics comics) {
        this.comics = comics;
        new Thread(() -> Utils.writeToFile(this.comics, comicsCacheFile)).start();
    }

    /**
     * Updates the instance of a particular comic in the local data-set.
     * @param comic The comic that needs to be updated.
     */
    public void updateComic(@NonNull Comic comic) {
        boolean updated = false;
        for(int i=0; i<comics.comics.size(); i++) {
            if(comics.comics.get(i).id.equals(comic.id)) {
                comics.comics.set(i, comic);
                updated = true;
                break;
            }
        }
        if(updated) {
            listener.onComicUpdated(comic);
            save(comics);
        }
    }

    /**
     * Updates the instance of a particular chapter in the local data-set.
     * @param chapter The chapter that needs to be updated.
     */
    public void updateChapter(@NonNull Chapter chapter) {
        boolean updated = false;
        for(Comic comic : comics.comics) {
            for(int i=0; i<comic.chapters.size(); i++) {
                if(comic.chapters.get(i).id.equals(chapter.id)) {
                    comic.chapters.set(i, chapter.getCopyWithoutRawPageData());
                    updated = true;
                    break;
                }
            }
        }
        if(updated)
            save(comics);
    }

    /**
     * Free-up all the references so that GC can collect the memory.
     */
    @Override
    public void dispose() {
        stopLoadingComics();
        comicsCacheFile = null;
        loader = null;
        searcher = null;
        sorter = null;
        filterer = null;
        listener = null;
        sortingOption = null;
        comics = null;
        selectedCategories = null;
    }
}
