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

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Page;
import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.RemoteConfig;
import com.hemendra.comicreader.model.source.comics.remote.ComicsParser;
import com.hemendra.comicreader.model.source.images.local.ImagesDB;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.model.utils.Utils;

import java.io.File;
import java.net.HttpURLConnection;

/**
 * A worker thread which downloads all the pages for any given chapter.
 */
public class ChapterPagesDownloader extends CustomAsyncTask<Void,Integer,Boolean> {

    private Context context;
    private OnChapterDownloadListener listener;
    private Chapter chapter;
    private FailureReason reason = FailureReason.UNKNOWN_REMOTE_ERROR;
    private int progress1 = 0;
    private int count = 0;
    private ImagesDB db;
    private HttpURLConnection connection = null;

    ChapterPagesDownloader(Context context,
                           OnChapterDownloadListener listener,
                           Chapter chapter) {
        this.context = context;
        this.listener = listener;
        this.chapter = chapter.getCopyWithoutRawPageData();
        this.db = new ImagesDB(context).open();
    }

    @Override
    public void cancel(boolean interrupt) {
        if (connection != null)
            connection.disconnect();
        super.cancel(interrupt);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if(chapter.pages.size() == 0) {
            String json = ContentDownloader.downloadAsString(RemoteConfig.buildChapterUrl(chapter.id),
                    new ConnectionCallback() {
                        @Override
                        public void onConnectionInitialized(HttpURLConnection conn) {
                            connection = conn;
                        }

                        @Override
                        public void onResponseCode(int code) {
                            switch (code) {
                                case HttpURLConnection.HTTP_NOT_FOUND:
                                    reason = FailureReason.API_MISSING;
                                    break;
                                default:
                                    reason = FailureReason.INVALID_RESPONSE_FROM_SERVER;
                            }
                        }
                    });
            if(json != null) {
                chapter = ComicsParser.parseChapterPagesFromJSON(chapter, json);
            }
        }
        //
        if(chapter != null && chapter.pages.size() > 0) {
            boolean allSuccess = true;
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/" + context.getPackageName() + "/cache/chapters");
            File file = new File(dir, chapter.id + ".obj");
            if (!file.exists() || file.length() == 0) {
                if (dir.exists() || dir.mkdirs()) {
                    progress1 = count = 0;
                    for (Page page : chapter.pages) {
                        if(isCancelled())
                            break;
                        progress1 = (int) Math.ceil(((float) count / (float) chapter.pages.size()) * 100f);
                        publishProgress(progress1, count, chapter.pages.size(), 0, page.number, 0);

                        String imgUrl = page.getImageUrl();

                        page.rawImageData = db.getPage(imgUrl);

                        if (!isCancelled()
                                && page.rawImageData == null || page.rawImageData.length == 0) {
                            byte[] bytes = ContentDownloader.downloadAsByteArray(imgUrl,
                                    new ConnectionCallback() {
                                        @Override
                                        public void onConnectionInitialized(HttpURLConnection conn) {
                                            connection = conn;
                                        }
                                        @Override
                                        public void onProgress(float progress, int totalLength) {
                                            publishProgress(progress1,
                                                    count, chapter.pages.size(),
                                                    (int)Math.ceil(progress), count+1, totalLength);
                                        }

                                        @Override
                                        public void onResponseCode(int code) {
                                            switch (code) {
                                                case HttpURLConnection.HTTP_NOT_FOUND:
                                                    reason = FailureReason.CONTENT_MISSING;
                                                    break;
                                                default:
                                                    reason = FailureReason.INVALID_RESPONSE_FROM_SERVER;
                                            }
                                        }
                                    });
                            if (!isCancelled()
                                    && bytes != null && bytes.length > 0
                                    && isValidImageData(bytes)) {
                                page.rawImageData = bytes;
                                db.insertPage(imgUrl, bytes);
                            } else {
                                allSuccess = false;
                                break;
                            }
                        }

                        count++;
                    }
                    if(allSuccess) {
                        allSuccess = Utils.writeToFile(chapter, file);
                    }
                }
            }
            return allSuccess;
        }
        return false;
    }

    private boolean isValidImageData(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length) != null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        listener.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        db.close();
        if(success) {
            listener.onChapterDownloaded(chapter);
        } else {
            listener.onFailedToDownloadChapter(reason);
        }
    }
}
