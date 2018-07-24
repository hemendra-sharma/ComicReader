package com.hemendra.comicreader.model.source.images;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public interface IImagesDataSourceListener {

    void onImageLoadingStarted(@NonNull String url);
    void onImageLoaded(@NonNull String url, @NonNull Bitmap image);
    void onFailedToLoadImage(@NonNull int reason);
    void onStoppedLoadingImage(@NonNull String url);

}
