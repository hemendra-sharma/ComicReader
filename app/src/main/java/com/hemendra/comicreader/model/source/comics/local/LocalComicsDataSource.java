package com.hemendra.comicreader.model.source.comics.local;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.Utils;

import java.io.File;

public class LocalComicsDataSource extends ComicsDataSource implements OnComicsLoadedListener {

    public File comicsCacheFile;
    private LocalComicsLoader loader;
    private ComicsSearcher searcher = null;
    private Comics comics = null;

    public LocalComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context, listener);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + getContext().getPackageName() + "/cache");
        comicsCacheFile = new File(dir, "comics.obj");
        loader = new LocalComicsLoader(this);
    }

    private boolean hasComics() {
        return comicsCacheFile.exists() && comicsCacheFile.length() > 0;
    }
    
    @Override
    public void loadComics() {
        if (hasComics()) {
            if(comics != null) {
                listener.onComicsLoaded(comics, SourceType.LOCAL);
            } else if(loader == null) {
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

    public void searchComics(String query) {
        if(comics != null) {
            if(searcher == null || !searcher.isExecuting()) {
                searcher = new ComicsSearcher(comics, this);
                searcher.execute(query);
                listener.onStartedLoadingComics();
            } else {
                listener.onFailedToLoadComics(FailureReason.ALREADY_LOADING);
            }
        } else {
            listener.onFailedToLoadComics(FailureReason.UNKNOWN_LOCAL_ERROR);
        }
    }

    @Override
    public void onComicsLoaded(Comics comics) {
        if (comics != null) {
            this.comics = comics;
            listener.onComicsLoaded(comics, SourceType.LOCAL);
        } else
            listener.onFailedToLoadComics(FailureReason.UNKNOWN_LOCAL_ERROR);
    }

    @Override
    public void onFailedToLoadComics(FailureReason reason) {
        listener.onFailedToLoadComics(reason);
    }

    @Override
    public void onComicDetailsLoaded(Comic comic) {

    }

    @Override
    public void onFailedToLoadComicDetails(FailureReason reason) {

    }

    @Override
    protected void stopLoadingComics() {
        if (loader != null && loader.isExecuting())
            loader.cancel(true);
    }

    public void deleteCache() {
        Utils.deleteFile(comicsCacheFile);
        comics = null;
    }

    public void save(@NonNull Comics comics) {
        this.comics = comics;
        new Thread(() -> Utils.writeToFile(comics, comicsCacheFile)).start();
    }

    @Override
    public void dispose() {
        stopLoadingComics();
        loader = null;
        listener = null;
        comics = null;
    }
}
