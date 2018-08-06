package com.hemendra.comicreader.model.source.comics.remote;

import android.content.Context;
import android.widget.ImageView;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.source.images.remote.OnChapterDownloadListener;
import com.hemendra.comicreader.model.utils.Utils;

public class RemoteComicsDataSource extends ComicsDataSource implements OnComicsLoadedListener {

    private RemoteComicsLoader comicsLoader;
    private RemoteComicDetailsLoader detailsLoader;
    private RemoteChapterPagesLoader chapterLoader;

    public RemoteComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context, listener);
        comicsLoader = new RemoteComicsLoader(this);
        detailsLoader = new RemoteComicDetailsLoader(this);
        chapterLoader = new RemoteChapterPagesLoader(this);
    }

    @Override
    public void loadComics() {
        if(Utils.isNetworkAvailable(getContext())) {
            if(!comicsLoader.isExecuting()) {
                comicsLoader.execute();
                listener.onStartedLoadingComics();
            } else {
                listener.onFailedToLoadComics(FailureReason.ALREADY_LOADING);
            }
        } else {
            listener.onFailedToLoadComics(FailureReason.NETWORK_UNAVAILABLE);
        }
    }

    @Override
    public void onComicsLoaded(Comics comics, SourceType sourceType) {
        if(comics != null)
            listener.onComicsLoaded(comics, sourceType);
        else
            listener.onFailedToLoadComics(FailureReason.UNKNOWN_REMOTE_ERROR);
    }

    @Override
    public void onFailedToLoadComics(FailureReason reason) {
        listener.onFailedToLoadComics(reason);
    }

    @Override
    protected void stopLoadingComics() {
        if(comicsLoader != null && comicsLoader.isExecuting())
            comicsLoader.cancel(true);
    }

    public void loadComicDetails(Comic comic) {
        if(Utils.isNetworkAvailable(getContext())) {
            if(!detailsLoader.isExecuting()) {
                detailsLoader.execute(comic);
                listener.onStartedLoadingComicDetails();
            } else {
                listener.onFailedToLoadComicDetails(FailureReason.ALREADY_LOADING);
            }
        } else {
            listener.onFailedToLoadComicDetails(FailureReason.NETWORK_UNAVAILABLE);
        }
    }

    @Override
    public void onComicDetailsLoaded(Comic comic) {
        listener.onComicDetailsLoaded(comic);
    }

    @Override
    public void onFailedToLoadComicDetails(FailureReason reason) {
        listener.onFailedToLoadComicDetails(reason);
    }

    public void loadPagesSilent(Chapter chapter, ImageView iv,
                                OnChapterDownloadListener listener) {
        if(Utils.isNetworkAvailable(getContext())) {
            if(!chapterLoader.isExecuting()) {
                chapterLoader.execute(chapter);
            } else {
                listener.onAlreadyLoading(chapter, iv);
            }
        } else {
            listener.onFailedToDownloadChapter(chapter, iv);
        }
    }

    public void loadPages(Chapter chapter) {
        if(Utils.isNetworkAvailable(getContext())) {
            if(!chapterLoader.isExecuting()) {
                chapterLoader.execute(chapter);
                listener.onStartedLoadingPages();
            } else {
                listener.onFailedToLoadComicDetails(FailureReason.ALREADY_LOADING);
            }
        } else {
            listener.onFailedToLoadComicDetails(FailureReason.NETWORK_UNAVAILABLE);
        }
    }

    @Override
    public void onPagesLoaded(Chapter chapter) {
        listener.onPagesLoaded(chapter);
    }

    @Override
    public void onFailedToLoadPages(FailureReason reason) {
        listener.onFailedToLoadPages(reason);
    }

    @Override
    public void dispose() {
        stopLoadingComics();
        listener = null;
        comicsLoader = null;
        detailsLoader = null;
    }
}
