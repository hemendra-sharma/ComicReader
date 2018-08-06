package com.hemendra.comicreader.presenter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import com.hemendra.comicreader.model.source.images.remote.OnPagesDownloadedListener;
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

    public boolean hasAllPagesOffline(Chapter chapter) {
        return localImagesDataSource != null
                && localImagesDataSource.isChapterAvailableOffline(chapter);
    }

    public void downloadAllPages(Chapter chapter, ImageView iv) {
        if(activityView != null
                && localImagesDataSource != null
                && remoteImagesDataSource != null
                && remoteComicsDataSource != null) {
            if(hasAllPagesOffline(chapter)) {
                if(chapter.equals(iv.getTag()))
                    iv.setImageResource(R.drawable.ic_check);
            } else if(chapter.pages.size() == 0){
                if(chapter.equals(iv.getTag())) {
                    iv.setImageResource(R.drawable.ic_wait);
                    remoteComicsDataSource.loadPagesSilent(chapter, iv, onChapterDownloadListener);
                }
            }
        }
    }

    private OnChapterDownloadListener onChapterDownloadListener = new OnChapterDownloadListener() {
        @Override
        public void onChapterDownloaded(Chapter chapter, ImageView iv) {
            if(activityView != null && chapter.equals(iv.getTag())) {
                if(localComicsDataSource != null)
                    localComicsDataSource.updateChapter(chapter);
                if(hasAllPagesOffline(chapter))
                    iv.setImageResource(R.drawable.ic_check);
                else
                    remoteImagesDataSource.loadPages(chapter, iv,
                            onPagesDownloadedListener);
            }
        }

        @Override
        public void onAlreadyLoading(Chapter chapter, ImageView iv) {
            if(activityView != null && chapter.equals(iv.getTag()))
                iv.setImageResource(R.drawable.ic_wait);
        }

        @Override
        public void onFailedToDownloadChapter(Chapter chapter, ImageView iv) {
            if(activityView != null && chapter.equals(iv.getTag()))
                iv.setImageResource(R.drawable.ic_download);
        }
    };

    private OnPagesDownloadedListener onPagesDownloadedListener = new OnPagesDownloadedListener() {
        @Override
        public void onPagesDownloaded(Chapter chapter, ImageView iv) {
            if(activityView != null && chapter.equals(iv.getTag()))
                iv.setImageResource(R.drawable.ic_check);
        }

        @Override
        public void onAlreadyLoading(Chapter chapter, ImageView iv) {
            if(activityView != null && chapter.equals(iv.getTag()))
                iv.setImageResource(R.drawable.ic_wait);
        }

        @Override
        public void onFailedToDownloadPages(Chapter chapter, ImageView iv) {
            if(activityView != null && chapter.equals(iv.getTag()))
                iv.setImageResource(R.drawable.ic_download);
        }
    };

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

        presenter = null;
    }
}
