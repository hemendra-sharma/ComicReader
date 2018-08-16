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

package com.hemendra.comicreader.model.source.comics;

import android.support.annotation.NonNull;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource.SourceType;

public interface IComicsDataSourceListener {

    void onStartedLoadingComics();
    void onComicsLoaded(@NonNull Comics comics, @NonNull SourceType sourceType);
    void onFailedToLoadComics(@NonNull FailureReason reason);

    void onStartedLoadingComicDetails();
    void onComicDetailsLoaded(Comic comic);
    void onComicUpdated(Comic comic);
    void onFailedToLoadComicDetails(FailureReason reason);

    void onStartedLoadingPages();
    void onPagesLoaded(Chapter chapter);
    void onFailedToLoadPages(FailureReason reason);
}
