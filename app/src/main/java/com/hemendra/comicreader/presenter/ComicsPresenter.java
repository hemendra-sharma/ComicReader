package com.hemendra.comicreader.presenter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.DataSource;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource.SourceType;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.comics.local.LocalComicsDataSource;
import com.hemendra.comicreader.model.source.comics.remote.RemoteComicsDataSource;
import com.hemendra.comicreader.model.source.images.IImagesDataSourceListener;
import com.hemendra.comicreader.model.source.images.local.LocalImagesDataSource;
import com.hemendra.comicreader.model.source.images.remote.OnChapterDownloadListener;
import com.hemendra.comicreader.model.source.images.remote.RemoteImagesDataSource;
import com.hemendra.comicreader.view.IComicListActivityCallback;
import com.hemendra.comicreader.view.list.SortingOption;
import com.hemendra.comicreader.view.reader.TouchImageView;

import java.util.ArrayList;

public class ComicsPresenter implements IComicsDataSourceListener, IImagesDataSourceListener {

    private static ComicsPresenter presenter = null;

    private Context context;
    private IComicListActivityCallback activityView;

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
            if(hasReadWritePermissions()) {
                localComicsDataSource.searchComics(query);
            } else {
                pendingAction = () -> performSearch(query);
                activityView.askForPermissions();
            }
        }
    }

    public void performSort(Comics comics, SortingOption sortingOption) {
        if(activityView != null
                && localComicsDataSource != null) {
            if(hasReadWritePermissions()) {
                localComicsDataSource.sortComics(comics, sortingOption);
            } else {
                pendingAction = () -> performSort(comics, sortingOption);
                activityView.askForPermissions();
            }
        }
    }

    public void performFilter(ArrayList<String> selectedCategories) {
        if(activityView != null
                && localComicsDataSource != null) {
            if(hasReadWritePermissions()) {
                localComicsDataSource.filterComics(selectedCategories);
            } else {
                pendingAction = () -> performFilter(selectedCategories);
                activityView.askForPermissions();
            }
        }
    }

    public void invalidateCacheAndLoadComicsAgain() {
        if(activityView != null
                && localComicsDataSource != null && remoteComicsDataSource != null) {
            localComicsDataSource.deleteCache();
            startLoadingComics();
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
            activityView.onComicsLoaded(comics);
            if(sourceType == SourceType.REMOTE) {
                localComicsDataSource.save(comics);
            }
        }
    }

    @Override
    public void onFailedToLoadComics(@NonNull FailureReason reason) {
        if(activityView != null) {
            if (reason == FailureReason.NOT_AVAILABLE_LOCALLY
                    || reason == FailureReason.UNKNOWN_LOCAL_ERROR) {
                if (remoteComicsDataSource != null) {
                    remoteComicsDataSource.loadComics();
                } else {
                    activityView.onFailedToLoadComics("App Destroyed");
                }
            } else if (reason == FailureReason.NETWORK_UNAVAILABLE) {
                activityView.onFailedToLoadComics("No Internet Connection");
            } else if (reason == FailureReason.NETWORK_TIMEOUT) {
                activityView.onFailedToLoadComics("Network Timeout");
            } else if (reason == FailureReason.ALREADY_LOADING) {
                // ignore
            } else {
                activityView.onFailedToLoadComics("Unknown");
            }
        }
    }

    public void loadImage(@NonNull String url, @NonNull ImageView iv) {
        if(activityView != null
                && localImagesDataSource != null) {
            localImagesDataSource.loadImage(url, iv);
        }
    }

    public void stopLoadingImageOrPage(@NonNull String url) {
        if(activityView != null
                && localImagesDataSource != null
                && remoteImagesDataSource != null) {
            localImagesDataSource.stopLoadingImage(url);
            remoteImagesDataSource.stopLoadingImage(url);
        }
    }

    @Override
    public void onImageLoaded(String url, Bitmap bmp) {
        if(localImagesDataSource != null) {
            localImagesDataSource.saveImage(url, bmp);
        }
    }

    @Override
    public void onFailedToLoadImage(@NonNull FailureReason reason, @NonNull String url,
                                    @NonNull ImageView iv) {
        if(reason == FailureReason.NOT_AVAILABLE_LOCALLY
                || reason == FailureReason.UNKNOWN_LOCAL_ERROR) {
            remoteImagesDataSource.loadImage(url, iv);
        }
    }

    public void loadPage(@NonNull String url, @NonNull TouchImageView iv) {
        if(activityView != null
                && localImagesDataSource != null) {
            localImagesDataSource.loadPage(url, iv);
        }
    }

    @Override
    public void onPageLoaded(String url, Bitmap bmp) {
        if(activityView != null
                && localImagesDataSource != null) {
            if(url != null && bmp != null)
                localImagesDataSource.savePage(url, bmp);
            activityView.onPageLoaded();
        }
    }

    @Override
    public void onFailedToLoadPage(@NonNull FailureReason reason, @NonNull String url,
                                   @NonNull TouchImageView iv) {
        if(reason == FailureReason.NOT_AVAILABLE_LOCALLY
                || reason == FailureReason.UNKNOWN_LOCAL_ERROR) {
            remoteImagesDataSource.loadPage(url, iv);
        }
    }

    public boolean isChapterOffline(Chapter chapter) {
        return localImagesDataSource != null
                && localImagesDataSource.isChapterOffline(chapter);
    }

    public Chapter getOfflineChapter(Chapter chapter) {
        if(localImagesDataSource != null) {
            return localImagesDataSource.getOfflineChapter(chapter);
        }
        return null;
    }

    public void downloadChapter(Chapter chapter, OnChapterDownloadListener listener) {
        if(activityView != null
                && localImagesDataSource != null
                && remoteImagesDataSource != null) {
            Chapter ch;
            if((ch = getOfflineChapter(chapter)) != null) {
                listener.onChapterDownloaded(ch);
            } else {
                remoteImagesDataSource.downloadChapter(chapter, listener);
            }
        }
    }

    public void stopDownloadingChapter() {
        if(remoteImagesDataSource != null) {
            remoteImagesDataSource.stopDownloadingChapter();
        }
    }

    public void loadComicDetails(Comic comic) {
        if(activityView != null && remoteComicsDataSource != null) {
            if(comic.chapters.size() > 0) {
                activityView.onComicDetailsLoaded(comic);
            } else {
                remoteComicsDataSource.loadComicDetails(comic);
            }
        }
    }

    public void setComicFavorite(Comic comic, boolean isFavorite) {
        if(activityView != null && localComicsDataSource != null) {
            comic.isFavorite = isFavorite;
            localComicsDataSource.updateComic(comic);
        }
    }

    @Override
    public void onStartedLoadingComicDetails() {
        if(activityView != null) {
            activityView.onComicDetailsLoadingStarted();
        }
    }

    @Override
    public void onComicDetailsLoaded(Comic comic) {
        if(activityView != null
                && localComicsDataSource != null) {
            activityView.onComicDetailsLoaded(comic);
            localComicsDataSource.updateComic(comic);
        }
    }

    @Override
    public void onFailedToLoadComicDetails(FailureReason reason) {
        if(activityView != null) {
            if(reason == FailureReason.NETWORK_UNAVAILABLE) {
                activityView.onFailedToLoadComicDetails("No Internet Connection");
            } else if(reason == FailureReason.NETWORK_TIMEOUT) {
                activityView.onFailedToLoadComicDetails("Network Timeout");
            } else if(reason == FailureReason.ALREADY_LOADING) {
                // ignore
            } else {
                activityView.onFailedToLoadComicDetails("Unknown");
            }
        }
    }

    public Chapter getNextChapterFrom(Chapter ch) {
        if(activityView != null) {
            return activityView.getNextChapterFromDetailsFragment(ch);
        }
        return null;
    }

    public void loadPages(Chapter chapter) {
        if(activityView != null
                && remoteComicsDataSource != null) {
            if(chapter.pages.size() > 0) {
                activityView.onChapterLoaded(chapter);
            } else {
                remoteComicsDataSource.loadPages(chapter);
            }
        }
    }

    public void loadPages(Chapter chapter, IComicsDataSourceListener listener) {
        if(chapter.pages.size() > 0) {
            activityView.onChapterLoadingStarted();
            listener.onPagesLoaded(chapter);
        } else {
            new RemoteComicsDataSource(context, listener).loadPages(chapter);
        }
    }

    public void showProgress() {
        if(activityView != null)
            activityView.showProgress();
    }

    public void hideProgress() {
        if(activityView != null)
            activityView.hideProgress();
    }

    @Override
    public void onStartedLoadingPages() {
        if(activityView != null) {
            activityView.onChapterLoadingStarted();
        }
    }

    @Override
    public void onPagesLoaded(Chapter chapter) {
        if(activityView != null
                && localComicsDataSource != null) {
            activityView.onChapterLoaded(chapter);
            updateChapter(chapter);
        }
    }

    public void updateChapter(Chapter chapter) {
        if(localComicsDataSource != null) {
            localComicsDataSource.updateChapter(chapter);
        }
    }

    @Override
    public void onFailedToLoadPages(FailureReason reason) {
        if(activityView != null) {
            if(reason == FailureReason.NETWORK_UNAVAILABLE) {
                activityView.onFailedToLoadComicDetails("No Internet Connection");
            } else if(reason == FailureReason.NETWORK_TIMEOUT) {
                activityView.onFailedToLoadComicDetails("Network Timeout");
            } else if(reason == FailureReason.ALREADY_LOADING) {
                // ignore
            } else {
                activityView.onFailedToLoadComicDetails("Unknown");
            }
        }
    }

    public void destroy() {
        activityView = null;

        for(DataSource source : sources)
            source.dispose();

        localComicsDataSource = null;
        remoteComicsDataSource = null;
        localImagesDataSource = null;
        remoteImagesDataSource = null;

        sources = null;

        presenter = null;
    }
}
