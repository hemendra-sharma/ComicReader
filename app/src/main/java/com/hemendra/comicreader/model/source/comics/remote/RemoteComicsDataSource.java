package com.hemendra.comicreader.model.source.comics.remote;

import android.content.Context;

import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;

public class RemoteComicsDataSource extends ComicsDataSource {

    public RemoteComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context, listener);
    }

    @Override
    public void loadComics() {

    }

    @Override
    public void stopLoadingComics() {

    }

    @Override
    public void dispose() {

    }
}
