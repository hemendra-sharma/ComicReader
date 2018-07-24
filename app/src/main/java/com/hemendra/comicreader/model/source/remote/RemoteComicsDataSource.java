package com.hemendra.comicreader.model.source.remote;

import android.content.Context;

import com.hemendra.comicreader.model.source.ComicsDataSource;
import com.hemendra.comicreader.model.source.IComicsDataSourceListener;

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
}
