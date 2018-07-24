package com.hemendra.comicreader.model.source.local;

import android.content.Context;
import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.ComicsDataSource;
import com.hemendra.comicreader.model.source.IComicsDataSourceListener;

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

}
