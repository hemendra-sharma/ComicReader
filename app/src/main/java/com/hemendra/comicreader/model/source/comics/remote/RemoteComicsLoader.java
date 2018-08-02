package com.hemendra.comicreader.model.source.comics.remote;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.RemoteConfig;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;

import java.net.HttpURLConnection;

public class RemoteComicsLoader extends CustomAsyncTask<Void,Void,Comics> {

    private OnComicsLoadedListener listener;
    private FailureReason reason = FailureReason.UNKNOWN_REMOTE_ERROR;

    public RemoteComicsLoader(OnComicsLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Comics doInBackground(Void... voids) {
        String json = ContentDownloader.downloadAsString(RemoteConfig.buildComicsUrl(-1),
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
            return ComicsParser.parseComicsFromJSON(json);
        }
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
