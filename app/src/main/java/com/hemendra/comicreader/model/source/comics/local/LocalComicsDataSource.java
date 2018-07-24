package com.hemendra.comicreader.model.source.comics.local;

import android.content.Context;

import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;

public class LocalComicsDataSource extends ComicsDataSource {

    public LocalComicsDataSource(Context context, IComicsDataSourceListener listener) {
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
