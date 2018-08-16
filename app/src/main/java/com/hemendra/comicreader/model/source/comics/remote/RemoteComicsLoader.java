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

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.RemoteConfig;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class RemoteComicsLoader extends CustomAsyncTask<Void,Void,Comics> {

    private OnComicsLoadedListener listener;
    private FailureReason reason = FailureReason.UNKNOWN_REMOTE_ERROR;

    public RemoteComicsLoader(OnComicsLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Comics doInBackground(Void... voids) {
        InputStream in = ContentDownloader.downloadAsStream(RemoteConfig.buildComicsUrl(-1),
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
        if(in != null) {
            Comics comics = ComicsParser.parseComicsFromJSON(in);
            if(comics != null) {
                return comics;
            }
        }

        /*String json = ContentDownloader.downloadAsString(RemoteConfig.buildComicsUrl(-1),
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
            Comics comics = ComicsParser.parseComicsFromJSON(json);
            if(comics != null) {
                comics.comics.sort((c1, c2) -> Integer.compare(c2.hits, c1.hits));
                return comics;
            }
        }*/
        return null;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        if(comics != null)
            listener.onComicsLoaded(comics, ComicsDataSource.SourceType.REMOTE);
        else
            listener.onFailedToLoadComics(reason);
    }
}
