package com.hemendra.comicreader.model.source.comics.remote;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.RemoteConfig;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;

import java.net.HttpURLConnection;

public class RemoteChapterPagesLoader extends CustomAsyncTask<Chapter,Void,Chapter> {

    private OnComicsLoadedListener listener;
    private FailureReason reason = FailureReason.UNKNOWN_REMOTE_ERROR;

    public RemoteChapterPagesLoader(OnComicsLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Chapter doInBackground(Chapter... params) {
        String json = ContentDownloader.downloadAsString(RemoteConfig.buildChapterUrl(params[0].id),
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
