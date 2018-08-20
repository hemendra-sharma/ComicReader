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
import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.ImageAndViewHolder;

/**
 * A worker thread to read from local databse and decode the data into Bitmap asynchronously.
 */
public class LocalImageLoader extends CustomAsyncTask<Void,Void,Bitmap> {

    private LocalImagesDataSource dataSource;
    public String imgUrl;
    private ImageAndViewHolder holder;
    public long startedAt = 0;

    LocalImageLoader(@NonNull LocalImagesDataSource dataSource,
                     String imgUrl, ImageAndViewHolder holder) {
        this.dataSource = dataSource;
        this.imgUrl = imgUrl;
        this.holder = holder;
    }

    @Override
    protected void onPreExecute() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        return dataSource.getImageFromCache(imgUrl);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(bitmap != null) {
            holder.setImage(bitmap);
            dataSource.onImageDownloaded(imgUrl, bitmap, holder.isCover(), holder.isPage());
        } else
            dataSource.onFailedToDownloadImage(imgUrl, holder);
    }
}
