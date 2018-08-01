package com.hemendra.comicreader.model.source.comics.remote;

import android.content.Context;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.Utils;

public class RemoteComicsDataSource extends ComicsDataSource implements OnComicsLoadedListener {

    private RemoteComicsLoader comicsLoader;
    private RemoteComicDetailsLoader detailsLoader;

    public RemoteComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context, listener);
        comicsLoader = new RemoteComicsLoader(this);
        detailsLoader = new RemoteComicDetailsLoader(this);
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
    public void onComicsLoaded(Comics comics) {
        if(comics != null)
            listener.onComicsLoaded(comics, SourceType.REMOTE);
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

    @Override
    public void dispose() {
        stopLoadingComics();
        listener = null;
        comicsLoader = null;
        detailsLoader = null;
    }
}
