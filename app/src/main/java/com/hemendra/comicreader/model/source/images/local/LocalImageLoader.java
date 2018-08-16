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
import android.widget.ImageView;

import com.hemendra.comicreader.model.utils.CustomAsyncTask;

public class LocalImageLoader extends CustomAsyncTask<Void,Void,Bitmap> {

    private LocalImagesDataSource dataSource;
    public String imgUrl;
    private ImageView iv;
    public long startedAt = 0;

    public LocalImageLoader(@NonNull LocalImagesDataSource dataSource,
                            String imgUrl, ImageView iv) {
        this.dataSource = dataSource;
        this.imgUrl = imgUrl;
        this.iv = iv;
    }

    @Override
    protected void onPreExecute() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            return dataSource.getImageFromCache(imgUrl);
        }catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(bitmap != null) {
            if(iv != null)
                iv.setImageBitmap(bitmap);
            dataSource.onImageDownloaded(imgUrl, bitmap, iv != null, false);
        } else
            dataSource.onFailedToDownloadImage(imgUrl, iv, null);
    }
}
