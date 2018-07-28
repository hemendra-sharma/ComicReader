package com.hemendra.comicreader.model.source.comics;

import android.content.Context;

import com.hemendra.comicreader.model.source.DataSource;

public abstract class ComicsDataSource extends DataSource {

    public enum FailureReason {
        NOT_AVAILABLE_LOCALLY,
        ALREADY_LOADING,
        SOURCE_CLOSED,
        NETWORK_UNAVAILABLE,
        NETWORK_TIMEOUT,
        API_MISSING,
        INVALID_RESPONSE_FROM_SERVER,
        UNKNOWN
    }

    public enum SourceType {
        LOCAL,
        REMOTE
    }

    protected IComicsDataSourceListener listener;

    public ComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context);
        this.listener = listener;
    }

    public abstract void loadComics();

    protected abstract void stopLoadingComics();

}
