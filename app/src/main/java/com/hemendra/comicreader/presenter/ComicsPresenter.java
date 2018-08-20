/*
 * Copyright (c) 2018 Hemendra Sharma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.hemendra.comicreader.model.source.images.remote.RemoteImagesDataSource;
import com.hemendra.comicreader.view.IComicListActivityCallback;
import com.hemendra.comicreader.view.ImageAndViewHolder;
import com.hemendra.comicreader.view.list.SortingOption;
import com.hemendra.comicreader.view.reader.TouchImageView;

import java.util.ArrayList;

/**
 * Handles the communication between the View and Model.
 * This class is responsible for all the operation related to providing input from user, and
 * data from model classes.
 */
public class ComicsPresenter implements IComicsPresenter, IComicsDataSourceListener, IImagesDataSourceListener {

    private Context context;
    private IComicListActivityCallback activityView;

    private DataSource[] sources;

    private LocalComicsDataSource localComicsDataSource;
    private RemoteComicsDataSource remoteComicsDataSource;

    private LocalImagesDataSource localImagesDataSource;
    private RemoteImagesDataSource remoteImagesDataSource;

    private Runnable pendingAction = null;

    public ComicsPresenter(Context context, IComicListActivityCallback activityView) {
        this.context = context;
        this.activityView = activityView;

        sources = new DataSource[]{
                localComicsDataSource = new LocalComicsDataSource(context, this),
                remoteComicsDataSource = new RemoteComicsDataSource(context, this),
                localImagesDataSource = new LocalImagesDataSource(context, this),
                remoteImagesDataSource = new RemoteImagesDataSource(context, this)
        };
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void invalidateCacheAndLoadComicsAgain() {
        if(activityView != null
                && localComicsDataSource != null && remoteComicsDataSource != null) {
            localComicsDataSource.deleteCache();
            startLoadingComics();
        }
    }

    @Override
    public void loadImage(@NonNull String url, @NonNull ImageAndViewHolder holder) {
        if(activityView != null
                && localImagesDataSource != null) {
            localImagesDataSource.loadImage(url, holder);
        }
    }

    @Override
    public void stopLoadingImageOrPage(@NonNull String url) {
        if(activityView != null
                && localImagesDataSource != null
                && remoteImagesDataSource != null) {
            localImagesDataSource.stopLoadingImage(url);
            remoteImagesDataSource.stopLoadingImage(url);
        }
    }

    @Override
    public void loadPage(@NonNull String url, @NonNull ImageAndViewHolder holder) {
        if(activityView != null
                && localImagesDataSource != null) {
            localImagesDataSource.loadPage(url, holder);
        }
    }

    @Override
    public boolean isChapterOffline(Chapter chapter) {
        return localImagesDataSource != null
                && localImagesDataSource.isChapterOffline(chapter);
    }

    @Override
    public Chapter getOfflineChapter(Chapter chapter) {
        if(localImagesDataSource != null) {
            return localImagesDataSource.getOfflineChapter(chapter);
        }
        return null;
    }

    @Override
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

    @Override
    public void stopDownloadingChapter() {
        if(remoteImagesDataSource != null) {
            remoteImagesDataSource.stopDownloadingChapter();
        }
    }

    @Override
    public void loadComicDetails(Comic comic) {
        if(activityView != null && remoteComicsDataSource != null) {
            if(comic.chapters.size() > 0) {
                activityView.onComicDetailsLoaded(comic);
            } else {
                remoteComicsDataSource.loadComicDetails(comic);
            }
        }
    }

    @Override
    public void setComicFavorite(Comic comic, boolean isFavorite) {
        if(activityView != null && localComicsDataSource != null) {
            comic.isFavorite = isFavorite;
            localComicsDataSource.updateComic(comic);
        }
    }

    @Override
    public Chapter getNextChapterFrom(Chapter ch) {
        if(activityView != null) {
            return activityView.getNextChapterFromDetailsFragment(ch);
        }
        return null;
    }

    @Override
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
    public void loadPages(Chapter chapter, IComicsDataSourceListener listener) {
        if(chapter.pages.size() > 0) {
            activityView.onChapterLoadingStarted();
            listener.onPagesLoaded(chapter);
        } else {
            new RemoteComicsDataSource(context, listener).loadPages(chapter);
        }
    }

    @Override
    public void showProgress() {
        if(activityView != null)
            activityView.showProgress();
    }

    @Override
    public void hideProgress() {
        if(activityView != null)
            activityView.hideProgress();
    }

    @Override
    public void updateChapter(Chapter chapter) {
        if(activityView != null
                && localComicsDataSource != null) {
            localComicsDataSource.updateChapter(chapter);
            activityView.refreshChaptersList();
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
                    activityView.onFailedToLoadComics(context.getString(R.string.app_closed));
                }
            } else if (reason == FailureReason.NETWORK_UNAVAILABLE) {
                activityView.onFailedToLoadComics(context.getString(R.string.no_internet_connection));
            } else if (reason == FailureReason.NETWORK_TIMEOUT) {
                activityView.onFailedToLoadComics(context.getString(R.string.connection_timeout));
            } else if (reason != FailureReason.ALREADY_LOADING) {
                activityView.onFailedToLoadComics(context.getString(R.string.unknown));
            }
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
                                    @NonNull ImageAndViewHolder holder) {
        if(reason == FailureReason.NOT_AVAILABLE_LOCALLY
                || reason == FailureReason.UNKNOWN_LOCAL_ERROR) {
            remoteImagesDataSource.loadImage(url, holder);
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
                                   @NonNull ImageAndViewHolder holder) {
        if(reason == FailureReason.NOT_AVAILABLE_LOCALLY
                || reason == FailureReason.UNKNOWN_LOCAL_ERROR) {
            remoteImagesDataSource.loadPage(url, holder);
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
    public void onComicUpdated(Comic comic) {
        if(activityView != null
                && localComicsDataSource != null) {
            activityView.onComicUpdated(comic);
        }
    }

    @Override
    public void onFailedToLoadComicDetails(FailureReason reason) {
        if(activityView != null) {
            if(reason == FailureReason.NETWORK_UNAVAILABLE) {
                activityView.onFailedToLoadComicDetails(context.getString(R.string.no_internet_connection));
            } else if(reason == FailureReason.NETWORK_TIMEOUT) {
                activityView.onFailedToLoadComicDetails(context.getString(R.string.connection_timeout));
            } else if (reason != FailureReason.ALREADY_LOADING) {
                activityView.onFailedToLoadComicDetails(context.getString(R.string.unknown));
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
            updateChapter(chapter);
        }
    }

    @Override
    public void onFailedToLoadPages(FailureReason reason) {
        if(activityView != null) {
            if(reason == FailureReason.NETWORK_UNAVAILABLE) {
                activityView.onFailedToLoadComicDetails(context.getString(R.string.no_internet_connection));
            } else if(reason == FailureReason.NETWORK_TIMEOUT) {
                activityView.onFailedToLoadComicDetails(context.getString(R.string.connection_timeout));
            } else if(reason != FailureReason.ALREADY_LOADING) {
                activityView.onFailedToLoadComicDetails(context.getString(R.string.unknown));
            }
        }
    }

    @Override
    public void destroy() {
        activityView = null;

        for(DataSource source : sources)
            source.dispose();

        localComicsDataSource = null;
        remoteComicsDataSource = null;
        localImagesDataSource = null;
        remoteImagesDataSource = null;

        sources = null;
    }
}
