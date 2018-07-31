package com.hemendra.comicreader.model.source.comics.remote;

import android.content.Context;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.Utils;

public class RemoteComicsDataSource extends ComicsDataSource implements OnComicsLoadedListener {

    private RemoteComicsLoader loader;

    public RemoteComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context, listener);
        loader = new RemoteComicsLoader(this);
    }

    @Override
    public void loadComics() {
        if(Utils.isNetworkAvailable(getContext())) {
            if(!loader.isExecuting()) {
                loader.execute();
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
        if(loader != null && loader.isExecuting())
            loader.cancel(true);
        listener.onStoppedLoadingComics();
    }

    @Override
    public void dispose() {
        stopLoadingComics();
        listener = null;
        loader = null;
    }
}
