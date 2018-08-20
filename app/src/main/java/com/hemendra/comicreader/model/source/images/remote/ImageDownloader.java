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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.ImageAndViewHolder;

import java.net.HttpURLConnection;

/**
 * A worker thread which downloads any image from given URL and converts it into Bitmap.
 */
public class ImageDownloader extends CustomAsyncTask<Void,Void,Bitmap> {

    private OnImageDownloadedListener listener;
    public String imgUrl;
    private ImageAndViewHolder holder;
    public long startedAt = 0;
    private HttpURLConnection connection = null;
    private Handler handler = null;

    ImageDownloader(OnImageDownloadedListener listener,
                    String imgUrl, ImageAndViewHolder holder) {
        this.listener = listener;
        this.imgUrl = imgUrl;
        this.holder = holder;
    }

    private Handler.Callback disconnectCallback = message -> {
        if(connection != null)
            connection.disconnect();
        return true;
    };

    @Override
    public void cancel(boolean interrupt) {
        if(handler != null) {
            handler.sendMessage(new Message());
        }
        super.cancel(interrupt);
    }

    @Override
    protected void onPreExecute() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Looper.prepare();
        byte[] bytes = ContentDownloader.downloadAsByteArray(imgUrl,
                new ConnectionCallback() {
                    @Override
                    public void onConnectionInitialized(HttpURLConnection conn) {
                        connection = conn;
                        handler = new Handler(disconnectCallback);
                    }
                });
        if (!isCancelled() && bytes != null && bytes.length > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inSampleSize = 1;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(bitmap != null) {
            holder.setImage(bitmap);
            if (listener != null)
                listener.onImageDownloaded(imgUrl, bitmap, holder.isCover(), holder.isPage());
        } else if (listener != null)
            listener.onFailedToDownloadImage(imgUrl, holder.isCover(), holder.isPage());
    }
}
