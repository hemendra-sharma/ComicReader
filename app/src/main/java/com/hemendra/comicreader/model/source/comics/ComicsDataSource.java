package com.hemendra.comicreader.model.source.comics;

import android.content.Context;

import com.hemendra.comicreader.model.source.DataSource;

public abstract class ComicsDataSource extends DataSource {

    public enum FailureReason {
        NOT_AVAILABLE_LOCALLY,
        NETWORK_TIMEOUT,
        UNKNOWN
    }

    protected IComicsDataSourceListener listener;

    public ComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context);
        this.listener = listener;
    }

    public abstract void loadComics();

    public abstract void stopLoadingComics();

}
