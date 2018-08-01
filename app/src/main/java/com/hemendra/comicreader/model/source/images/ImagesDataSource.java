package com.hemendra.comicreader.model.source.images;

import android.content.Context;
import android.widget.ImageView;

import com.hemendra.comicreader.model.source.DataSource;

public abstract class ImagesDataSource extends DataSource {

    protected IImagesDataSourceListener listener;

    public ImagesDataSource(Context context, IImagesDataSourceListener listener) {
        super(context);
        this.listener = listener;
    }

    public abstract void loadImage(String url, ImageView iv);

    public abstract void stopLoadingImage(String url);

}
