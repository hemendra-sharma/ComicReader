/*
 * Copyright (c) 2018 Hemendra Sharma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hemendra.comicreader.model.source.images;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.view.ImageAndViewHolder;

/**
 * Provides an interface for loading the images using any of the
 * {@link com.hemendra.comicreader.model.source.images.local.LocalImagesDataSource}
 * or {@link com.hemendra.comicreader.model.source.images.remote.RemoteImagesDataSource}
 */
public interface IImagesDataSourceListener {

    /**
     * Gets called when the cover image was loaded successfully.
     * @param url The URL used to load image.
     * @param bmp Loaded Bitmap.
     */
    void onImageLoaded(String url, Bitmap bmp);

    /**
     * Gets called when the cover image loading failed.
     * @param reason Any one of the {@link FailureReason}
     * @param url The URL used to load image.
     * @param holder The view holder which will handle this image after getting it.
     */
    void onFailedToLoadImage(@NonNull FailureReason reason,
                             @NonNull String url, @NonNull ImageAndViewHolder holder);

    /**
     * Gets called when the page was loaded successfully.
     * @param url The URL used to load page.
     * @param bmp Loaded Bitmap.
     */
    void onPageLoaded(String url, Bitmap bmp);

    /**
     * Gets called when the page loading failed.
     * @param reason Any one of the {@link FailureReason}
     * @param url The URL used to load image.
     * @param holder The view holder which will handle this image after getting it.
     */
    void onFailedToLoadPage(@NonNull FailureReason reason,
                            @NonNull String url, @NonNull ImageAndViewHolder holder);

}
