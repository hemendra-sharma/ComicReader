package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.model.utils.Utils;

import java.io.File;
import java.util.Collections;

public class LocalComicsLoader extends CustomAsyncTask<File,Void,Comics> {

    private OnComicsLoadedListener listener;
    private FailureReason reason = FailureReason.UNKNOWN_LOCAL_ERROR;

    public LocalComicsLoader(OnComicsLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Comics doInBackground(File... files) {
        if(files.length > 0 && files[0] != null && files[0].exists() && files[0].length() > 0) {
            Comics comics = (Comics) Utils.readObjectFromFile(files[0]);
            if(comics != null) {
                Collections.sort(comics.comics, (c1, c2) -> Integer.compare(c2.hits, c1.hits));
                return comics;
            }
        } else
            reason = FailureReason.NOT_AVAILABLE_LOCALLY;
        return null;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        if(comics != null)
            listener.onComicsLoaded(comics, ComicsDataSource.SourceType.LOCAL_FULL);
        else
            listener.onFailedToLoadComics(reason);
    }
}
