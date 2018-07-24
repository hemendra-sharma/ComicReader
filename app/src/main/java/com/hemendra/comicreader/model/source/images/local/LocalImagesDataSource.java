package com.hemendra.comicreader.model.source.images.local;

import android.content.Context;

import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.ImagesDataSource;

public class LocalImagesDataSource extends ImagesDataSource {

    public LocalImagesDataSource(Context context, IImagesDataSourceListener listener) {
        super(context, listener);
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
