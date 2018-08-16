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

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.RemoteConfig;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;

import java.net.HttpURLConnection;

/**
 * A background worker thread to download the comic details and the list of chapters for any
 * given comic.
 * @author Hemendra Sharma
 * @see CustomAsyncTask
 */
public class RemoteComicDetailsLoader extends CustomAsyncTask<Comic,Void,Comic> {

    private OnComicsLoadedListener listener;
    private FailureReason reason = FailureReason.UNKNOWN_REMOTE_ERROR;

    public RemoteComicDetailsLoader(OnComicsLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Comic doInBackground(Comic... comics) {
        String json = ContentDownloader.downloadAsString(
                RemoteConfig.buildComicDetailsUrl(comics[0].id),
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
            return ComicsParser.parseChaptersFromJSON(comics[0], json);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Comic comic) {
        if(listener != null) {
            if(comic != null)
                listener.onComicDetailsLoaded(comic);
            else
                listener.onFailedToLoadComicDetails(reason);
        }
    }
}
