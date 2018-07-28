package com.hemendra.comicreader.model.source.comics.local;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.Utils;

import java.io.File;

public class LocalComicsDataSource extends ComicsDataSource implements OnComicsLoadedListener {

    public File comicsCacheFile;
    private LocalComicsLoader loader;

    public LocalComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context, listener);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + getContext().getPackageName() + "/cache");
        comicsCacheFile = new File(dir, "comics.obj");
        loader = new LocalComicsLoader(this);
    }

    private boolean hasComics() {
        return comicsCacheFile.exists() && comicsCacheFile.length() > 0;
    }
    
    @Override
    public void loadComics() {
        if (hasComics()) {
            if(loader == null) {
                listener.onFailedToLoadComics(FailureReason.SOURCE_CLOSED);
            } else if (!loader.isExecuting()) {
                loader.execute(comicsCacheFile);
                listener.onStartedLoadingComics();
            } else {
                listener.onFailedToLoadComics(FailureReason.ALREADY_LOADING);
            }
        } else {
            listener.onFailedToLoadComics(FailureReason.NOT_AVAILABLE_LOCALLY);
        }
    }

    @Override
    public void onComicsLoaded(Comics comics) {
        if (comics != null)
            listener.onComicsLoaded(comics, SourceType.LOCAL);
        else
            listener.onFailedToLoadComics(FailureReason.UNKNOWN);
    }

    @Override
    public void onFailedToLoadComics(FailureReason reason) {
        listener.onFailedToLoadComics(reason);
    }

    @Override
    protected void stopLoadingComics() {
        if (loader != null && loader.isExecuting())
            loader.cancel(true);
        listener.onStoppedLoadingComics();
    }

    public void save(@NonNull Comics comics) {
        new Thread(() -> Utils.writeToFile(comics, comicsCacheFile)).start();
    }

    @Override
    public void dispose() {
        stopLoadingComics();
        loader = null;
        listener = null;
    }
}
