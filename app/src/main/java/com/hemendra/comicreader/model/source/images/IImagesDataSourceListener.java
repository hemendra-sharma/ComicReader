package com.hemendra.comicreader.model.source.images;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;

public interface IImagesDataSourceListener {

    void onImageLoaded(String url, Bitmap bmp);
    void onFailedToLoadImage(@NonNull FailureReason reason,
                             @NonNull String url, @NonNull ImageView iv);

}
