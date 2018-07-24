package com.hemendra.comicreader.model.source.images.remote;

import android.content.Context;

import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.ImagesDataSource;

public class RemoteImagesDataSource extends ImagesDataSource {

    private IImagesDataSourceListener listener;

    public RemoteImagesDataSource(Context context, IImagesDataSourceListener listener) {
        super(context, listener);
        this.listener = listener;
    }

    @Override
    public void loadImage(String url) {

    }

    @Override
    public void stopLoadingImage(String url) {

    }

    @Override
    public void dispose() {

    }
}
