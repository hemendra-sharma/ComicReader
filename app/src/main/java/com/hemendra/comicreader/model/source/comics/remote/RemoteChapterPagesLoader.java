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

package com.hemendra.comicreader.model.source.comics.remote;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.RemoteConfig;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;

import java.net.HttpURLConnection;

/**
 * A background worker thread to download the list of pages for any given chapter.
 * @author Hemendra Sharma
 * @see CustomAsyncTask
 */
public class RemoteChapterPagesLoader extends CustomAsyncTask<Chapter,Void,Chapter> {

    private OnComicsLoadedListener listener;
    private FailureReason reason = FailureReason.UNKNOWN_REMOTE_ERROR;

    RemoteChapterPagesLoader(OnComicsLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Chapter doInBackground(Chapter... params) {
        String json = ContentDownloader.downloadAsString(
                RemoteConfig.buildChapterUrl(params[0].id),
                new ConnectionCallback() {
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
            return ComicsParser.parseChapterPagesFromJSON(params[0], json);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Chapter chapter) {
        if(listener != null) {
            if(chapter != null)
                listener.onPagesLoaded(chapter);
            else
                listener.onFailedToLoadPages(reason);
        }
    }

}
