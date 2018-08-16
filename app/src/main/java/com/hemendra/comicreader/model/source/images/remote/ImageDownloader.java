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

package com.hemendra.comicreader.model.source.images.remote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.reader.TouchImageView;

import java.net.HttpURLConnection;

public class ImageDownloader extends CustomAsyncTask<Void,Void,Bitmap> {

    private OnImageDownloadedListener listener;
    public String imgUrl;
    private ImageView iv;
    private TouchImageView tiv;
    public long startedAt = 0;
    private HttpURLConnection connection = null;

    public ImageDownloader(OnImageDownloadedListener listener,
                           String imgUrl, ImageView iv, TouchImageView tiv) {
        this.listener = listener;
        this.imgUrl = imgUrl;
        this.iv = iv;
        this.tiv = tiv;
    }

    @Override
    public void cancel(boolean interrupt) {
        try{
            if(connection != null)
                connection.disconnect();
        }catch (Throwable ex) {
            ex.printStackTrace();
        }
        super.cancel(interrupt);
    }

    @Override
    protected void onPreExecute() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            byte[] bytes = ContentDownloader.downloadAsByteArray(imgUrl,
                    new ConnectionCallback() {
                        @Override
                        public void onConnectionInitialized(HttpURLConnection conn) {
                            connection = conn;
                        }
                    });
            if (!isCancelled() && bytes != null && bytes.length > 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                options.inSampleSize = 1;
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            }
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
            else if(tiv != null) {
                tiv.setImageBitmap(bitmap);
                tiv.setTag(-1);
            }
            //
            if (listener != null)
                listener.onImageDownloaded(imgUrl, bitmap, iv != null, tiv != null);
        } else if (listener != null)
            listener.onFailedToDownloadImage(imgUrl, iv != null, tiv != null);
    }
}
