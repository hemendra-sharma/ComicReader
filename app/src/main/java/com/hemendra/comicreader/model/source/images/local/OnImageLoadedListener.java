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

package com.hemendra.comicreader.model.source.images.local;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.hemendra.comicreader.view.reader.TouchImageView;

/**
 * Provides an interface for {@link LocalImageLoader} callbacks.
 */
interface OnImageLoadedListener {

    /**
     * Gets called when image or page was loaded successfully.
     * @param url The URL from which image was loaded
     * @param bmp The bitmap that was loaded
     * @param image It was an Image?
     * @param page It was a Page?
     */
    void onImageDownloaded(String url, Bitmap bmp, boolean image, boolean page);

    /**
     * Gets called when it failed to load the image or page.
     * @param url The URL which was attempted to download
     * @param iv The view for cover image
     * @param tiv The view for page
     */
    void onFailedToDownloadImage(String url, ImageView iv, TouchImageView tiv);

}
