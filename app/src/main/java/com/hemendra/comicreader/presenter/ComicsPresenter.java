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
import com.hemendra.comicreader.model.source.comics.ComicsDataSource.SourceType;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.comics.local.LocalComicsDataSource;
import com.hemendra.comicreader.model.source.comics.remote.RemoteComicsDataSource;
import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.local.LocalImagesDataSource;
import com.hemendra.comicreader.model.source.images.remote.RemoteImagesDataSource;
import com.hemendra.comicreader.view.IComicListActivityCallback;

public class ComicsPresenter implements IComicsDataSourceListener, IImagesDataSourceListener {

    private static ComicsPresenter presenter = null;

    private Context context;
    private IComicListActivityCallback activityView;

    private Comics comics = null;
    private DataSource[] sources;

    private LocalComicsDataSource localComicsDataSource;
    private RemoteComicsDataSource remoteComicsDataSource;

    private LocalImagesDataSource localImagesDataSource;
    private RemoteImagesDataSource remoteImagesDataSource;

    private Runnable pendingAction = null;

    public static ComicsPresenter getInstance(Context context, IComicListActivityCallback activityView) {
        if(presenter == null) {
            presenter = new ComicsPresenter(context, activityView);
        }
        return presenter;
    }

    private ComicsPresenter(Context context, IComicListActivityCallback activityView) {
        this.context = context;
        this.activityView = activityView;

        sources = new DataSource[]{
                localComicsDataSource = new LocalComicsDataSource(context, this),
                remoteComicsDataSource = new RemoteComicsDataSource(context, this),
                localImagesDataSource = new LocalImagesDataSource(context, this),
                remoteImagesDataSource = new RemoteImagesDataSource(context, this)
        };
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
                && localComicsDataSource != null && remoteComicsDataSource != null) {
            if(hasReadWritePermissions()) {
                localComicsDataSource.loadComics();
            } else {
                pendingAction = this::startLoadingComics;
                activityView.askForPermissions();
            }
        }
    }

    public void performSearch(String query) {
        if(activityView != null
                && localComicsDataSource != null && remoteComicsDataSource != null) {

        }
    }

    @Override
    public void onStartedLoadingComics() {
        if(activityView != null) {
            activityView.onComicsLoadingStarted();
        }
    }

    @Override
    public void onComicsLoaded(@NonNull Comics comics, @NonNull SourceType sourceType) {
        if(activityView != null) {
            this.comics = comics;
            activityView.onComicsLoaded(comics);
            if(sourceType == SourceType.REMOTE) {
                localComicsDataSource.save(comics);
            }
        }
    }

    @Override
    public void onFailedToLoadComics(@NonNull FailureReason reason) {
        if(reason == FailureReason.NOT_AVAILABLE_LOCALLY
                || reason == FailureReason.UNKNOWN_LOCAL_ERROR) {
            if(remoteComicsDataSource != null) {
                remoteComicsDataSource.loadComics();
            } else if(activityView != null) {
                activityView.onFailedToLoadComics("App Destroyed");
            }
        } else if(reason == FailureReason.ALREADY_LOADING) {
            if(activityView != null) {
                activityView.onFailedToLoadComics("Comics Already Loading");
            }
        } else if(reason == FailureReason.NETWORK_UNAVAILABLE) {
            if(activityView != null) {
                activityView.onFailedToLoadComics("No Internet Connection");
            }
        } else if(reason == FailureReason.NETWORK_TIMEOUT) {
            if(activityView != null) {
                activityView.onFailedToLoadComics("Network Timeout");
            }
        } else if(activityView != null) {
            activityView.onFailedToLoadComics("Unknown");
        }
    }

    @Override
    public void onStoppedLoadingComics() {
        if(activityView != null) {
            activityView.onStoppedLoadingComics();
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

        for(DataSource source : sources)
            source.dispose();

        localComicsDataSource = null;
        remoteComicsDataSource = null;
        localImagesDataSource = null;
        remoteImagesDataSource = null;

        presenter = null;
    }
}
