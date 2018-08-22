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

package com.hemendra.comicreader.model.source.comics.remote;

import android.content.Context;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.Utils;

/**
 * All the operations which are related to downloading and parsing comics data from
 * remote API, are handled in this class.
 * @author Hemendra Sharma
 */
public class RemoteComicsDataSource extends ComicsDataSource implements OnComicsLoadedListener {

    private RemoteComicsLoader comicsLoader;
    private RemoteComicDetailsLoader detailsLoader;
    private RemoteChapterPagesLoader chapterLoader;

    /**
     * Creates a new instance of {@link RemoteComicsDataSource}
     * @param context The android application context
     * @param listener An instance of {@link IComicsDataSourceListener} which in this case,
     *                 is {@link com.hemendra.comicreader.presenter.ComicsPresenter}
     */
    public RemoteComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context, listener);
        comicsLoader = new RemoteComicsLoader(this);
        detailsLoader = new RemoteComicDetailsLoader(this);
        chapterLoader = new RemoteChapterPagesLoader(this);
    }

    /**
     * Triggers the background thread which loads comics list from remote server asynchronously.
     */
    @Override
    public void loadComics() {
        if(listener != null) {
            if (Utils.isNetworkAvailable(getContext())) {
                if (!comicsLoader.isExecuting()) {
                    comicsLoader.execute();
                    listener.onStartedLoadingComics();
                } else {
                    listener.onFailedToLoadComics(FailureReason.ALREADY_LOADING);
                }
            } else {
                listener.onFailedToLoadComics(FailureReason.NETWORK_UNAVAILABLE);
            }
        }
    }

    @Override
    public void onComicsLoaded(Comics comics, SourceType sourceType) {
        if(listener != null) {
            if (comics != null)
                listener.onComicsLoaded(comics, sourceType);
            else
                listener.onFailedToLoadComics(FailureReason.UNKNOWN_REMOTE_ERROR);
        }
    }

    @Override
    public void onFailedToLoadComics(FailureReason reason) {
        if(listener != null) listener.onFailedToLoadComics(reason);
    }

    @Override
    protected void stopLoadingComics() {
        if(comicsLoader != null && comicsLoader.isExecuting())
            comicsLoader.cancel(true);
    }

    /**
     * Triggers the background thread which loads comic details and chapters' list from
     * remote server asynchronously.
     * @param comic The comic instance for which details are to be loaded.
     */
    public void loadComicDetails(Comic comic) {
        if(listener != null) {
            if (Utils.isNetworkAvailable(getContext())) {
                if (!detailsLoader.isExecuting()) {
                    detailsLoader.execute(comic);
                    listener.onStartedLoadingComicDetails();
                } else {
                    listener.onFailedToLoadComicDetails(FailureReason.ALREADY_LOADING);
                }
            } else {
                listener.onFailedToLoadComicDetails(FailureReason.NETWORK_UNAVAILABLE);
            }
        }
    }

    @Override
    public void onComicDetailsLoaded(Comic comic) {
        if(listener != null) listener.onComicDetailsLoaded(comic);
    }

    @Override
    public void onFailedToLoadComicDetails(FailureReason reason) {
        if(listener != null) listener.onFailedToLoadComicDetails(reason);
    }

    /**
     * Triggers the background thread which loads the list of pages from remote server
     * asynchronously.
     * @param chapter The chapter instance for which the list of pages are to be loaded.
     */
    public void loadPages(Chapter chapter) {
        if(listener != null) {
            if (Utils.isNetworkAvailable(getContext())) {
                if (!chapterLoader.isExecuting()) {
                    chapterLoader.execute(chapter);
                    listener.onStartedLoadingPages();
                } else {
                    listener.onFailedToLoadComicDetails(FailureReason.ALREADY_LOADING);
                }
            } else {
                listener.onFailedToLoadComicDetails(FailureReason.NETWORK_UNAVAILABLE);
            }
        }
    }

    @Override
    public void onPagesLoaded(Chapter chapter) {
        if(listener != null) listener.onPagesLoaded(chapter);
    }

    @Override
    public void onFailedToLoadPages(FailureReason reason) {
        if(listener != null) listener.onFailedToLoadPages(reason);
    }

    /**
     * Stops all asynchronous tasks and free up the reference so that GC can collect memory.
     */
    @Override
    public void dispose() {
        stopLoadingComics();
        listener = null;
        comicsLoader = null;
        detailsLoader = null;
    }
}
