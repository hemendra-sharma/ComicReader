package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.model.utils.Utils;

import java.io.File;

public class LocalComicsLoader extends CustomAsyncTask<File,Void,Comics> {

    private OnComicsLoadedListener listener;

    public LocalComicsLoader(OnComicsLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Comics doInBackground(File... files) {
        return (Comics) Utils.readObjectFromFile(files[0]);
    }

    @Override
    protected void onPostExecute(Comics comics) {
        listener.onComicsLoaded(comics);
    }
}
