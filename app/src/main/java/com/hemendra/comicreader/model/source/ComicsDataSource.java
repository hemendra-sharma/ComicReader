package com.hemendra.comicreader.model.source;

import android.content.Context;

import com.hemendra.comicreader.model.data.Comics;

public abstract class ComicsDataSource {

    private Comics comics = null;
    private Context context;
    protected IComicsDataSourceListener listener;

    public ComicsDataSource(Context context, IComicsDataSourceListener listener) {
        this.context = context;
        this.listener = listener;
    }

    protected Context getContext() {
        return this.context;
    }

    public abstract void loadComics();

    public abstract void stopLoadingComics();

}
