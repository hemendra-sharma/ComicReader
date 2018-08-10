package com.hemendra.comicreader;

import android.app.Application;

import com.hemendra.comicreader.model.source.images.remote.RemoteImagesDataSource;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        RemoteImagesDataSource.MAX_PARALLEL_DOWNLOADS = getResources().getInteger(R.integer.main_recycler_span_count) * 5;
        super.onCreate();
    }
}
