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

import android.content.Context;
import android.widget.ImageView;

import com.hemendra.comicreader.model.source.DataSource;
import com.hemendra.comicreader.view.reader.TouchImageView;

public abstract class ImagesDataSource extends DataSource {

    protected IImagesDataSourceListener listener;

    protected ImagesDataSource(Context context, IImagesDataSourceListener listener) {
        super(context);
        this.listener = listener;
    }

    public abstract void loadImage(String url, ImageView iv);

    public abstract void loadPage(String url, TouchImageView iv);

    public abstract void stopLoadingImage(String url);

}
