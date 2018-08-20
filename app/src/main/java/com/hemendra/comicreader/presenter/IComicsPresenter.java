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

import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.model.source.images.remote.OnChapterDownloadListener;
import com.hemendra.comicreader.view.ImageAndViewHolder;
import com.hemendra.comicreader.view.list.SortingOption;

import java.util.ArrayList;

/**
 * Provides an interface for the presenter
 * @author Hemendra Sharma
 */
public interface IComicsPresenter {

    void permissionGranted();
    void startLoadingComics();
    void performSearch(String query);
    void performSort(Comics comics, SortingOption sortingOption);
    void performFilter(ArrayList<String> selectedCategories);
    void invalidateCacheAndLoadComicsAgain();
    void loadImage(@NonNull String url, @NonNull ImageAndViewHolder holder);
    void stopLoadingImageOrPage(@NonNull String url);
    void loadPage(@NonNull String url, @NonNull ImageAndViewHolder holder);
    boolean isChapterOffline(Chapter chapter);
    Chapter getOfflineChapter(Chapter chapter);
    void downloadChapter(Chapter chapter, OnChapterDownloadListener listener);
    void stopDownloadingChapter();
    void loadComicDetails(Comic comic);
    void setComicFavorite(Comic comic, boolean isFavorite);
    Chapter getNextChapterFrom(Chapter ch);
    void loadPages(Chapter chapter);
    void loadPages(Chapter chapter, IComicsDataSourceListener listener);
    void showProgress();
    void hideProgress();
    void updateChapter(Chapter chapter);
    void destroy();
}
