package com.hemendra.comicreader.presenter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.DataSource;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource.FailureReason;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.comics.local.LocalComicsDataSource;
import com.hemendra.comicreader.model.source.comics.remote.RemoteComicsDataSource;
import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.local.LocalImagesDataSource;
import com.hemendra.comicreader.model.source.images.remote.RemoteImagesDataSource;
import com.hemendra.comicreader.view.IComicListActivityCallback;
import com.hemendra.comicreader.view.details.IComicDetailsCallback;
import com.hemendra.comicreader.view.list.IComicsListCallback;
import com.hemendra.comicreader.view.reader.IComicReaderCallback;

public class ComicsPresenter implements IComicsDataSourceListener, IImagesDataSourceListener {

    private static ComicsPresenter presenter = null;

    private Context context;
    private IComicListActivityCallback activityView;
    private IComicsListCallback listView;
    private IComicDetailsCallback detailsView;
    private IComicReaderCallback readerView;

    private Comics comics = null;
    private DataSource[] sources;

    private LocalComicsDataSource localComicsDataSource;
    private RemoteComicsDataSource remoteComicsDataSource;

    private LocalImagesDataSource localImagesDataSource;
    private RemoteImagesDataSource remoteImagesDataSource;

    private Runnable pendingAction = null;

    public static ComicsPresenter getInstance(Context context) {
        if(presenter == null) {
            presenter = new ComicsPresenter(context);
        }
        return presenter;
    }

    private ComicsPresenter(Context context) {
        this.context = context;

        sources = new DataSource[]{
                localComicsDataSource = new LocalComicsDataSource(context, this),
                remoteComicsDataSource = new RemoteComicsDataSource(context, this),
                localImagesDataSource = new LocalImagesDataSource(context, this),
                remoteImagesDataSource = new RemoteImagesDataSource(context, this)
        };
    }

    public void setActivityView(IComicListActivityCallback activityView) {
        this.activityView = activityView;
    }

    public void setListView(IComicsListCallback listView) {
        this.listView = listView;
    }

    public void setDetailsView(IComicDetailsCallback detailsView) {
        this.detailsView = detailsView;
    }

    public void setReaderView(IComicReaderCallback readerView) {
        this.readerView = readerView;
    }

    public void permissionGranted() {
        if(pendingAction != null) {
            pendingAction.run();
            pendingAction = null;
        }
    }

    private boolean hasReadWritePermissions() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void startLoadingComics() {
        if(activityView != null
                && localComicsDataSource != null && remoteImagesDataSource != null) {
            if(hasReadWritePermissions()) {
                localComicsDataSource.loadComics();
            } else {
                pendingAction = this::startLoadingComics;
                activityView.askForPermissions();
            }
        }
    }

    @Override
    public void onStartedLoadingComics() {
        if(listView != null) {
            listView.onComicsLoadingStarted();
        }
    }

    @Override
    public void onComicsLoaded(@NonNull Comics comics) {
        if(listView != null) {
            this.comics = comics;
            listView.onComicsLoaded(comics);
        }
    }

    @Override
    public void onFailedToLoadComics(@NonNull FailureReason reason) {
        if(reason == FailureReason.NOT_AVAILABLE_LOCALLY) {
            if(remoteComicsDataSource != null) {
                remoteComicsDataSource.loadComics();
            } else if(listView != null) {
                listView.onFailedToLoadComics("App Destroyed");
            }
        } else if(reason == FailureReason.NETWORK_TIMEOUT) {
            if(listView != null) {
                listView.onFailedToLoadComics("Network Timeout");
            }
        } else if(listView != null) {
            listView.onFailedToLoadComics("Unknown");
        }
    }

    @Override
    public void onStoppedLoadingComics() {
        if(listView != null) {
            listView.onStoppedLoadingComics();
        }
    }

    @Override
    public void onImageLoadingStarted(@NonNull String url) {

    }

    @Override
    public void onImageLoaded(@NonNull String url, @NonNull Bitmap image) {

    }

    @Override
    public void onFailedToLoadImage(@NonNull int reason) {

    }

    @Override
    public void onStoppedLoadingImage(@NonNull String url) {

    }

    public void destroy() {
        activityView = null;
        listView = null;
        detailsView = null;
        readerView = null;

        for(DataSource source : sources)
            source.dispose();

        localComicsDataSource = null;
        remoteComicsDataSource = null;
        localImagesDataSource = null;
        remoteImagesDataSource = null;
    }
}
