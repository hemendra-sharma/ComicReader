package com.hemendra.comicreader.model.source.comics.remote;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.RemoteConfig;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;

import java.net.HttpURLConnection;

public class RemoteComicDetailsLoader extends CustomAsyncTask<Comic,Void,Comic> {

    private OnComicsLoadedListener listener;
    private FailureReason reason = FailureReason.UNKNOWN_REMOTE_ERROR;

    public RemoteComicDetailsLoader(OnComicsLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Comic doInBackground(Comic... comics) {
        String json = ContentDownloader.downloadAsString(RemoteConfig.buildComicDetailsUrl(comics[0].id),
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