package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.model.utils.Utils;

import java.io.File;

public class LocalComicsLoader extends CustomAsyncTask<File,Void,Comics> {

    private OnComicsLoadedListener listener;
    private ComicsDataSource.FailureReason reason = ComicsDataSource.FailureReason.UNKNOWN;

    public LocalComicsLoader(OnComicsLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Comics doInBackground(File... files) {
        if(files.length > 0 && files[0] != null && files[0].exists() && files[0].length() > 0)
            return (Comics) Utils.readObjectFromFile(files[0]);
        else
            reason = ComicsDataSource.FailureReason.NOT_AVAILABLE_LOCALLY;
        return null;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        if(comics != null)
            listener.onComicsLoaded(comics);
        else
            listener.onFailedToLoadComics(reason);
    }
}
