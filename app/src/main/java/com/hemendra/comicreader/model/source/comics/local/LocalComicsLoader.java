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

package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.model.utils.Utils;
import com.hemendra.comicreader.view.list.SortingOption;

import java.io.File;
import java.util.Collections;

/**
 * Read the comics from local storage and load it up into memory.
 * @author Hemendra Sharma
 * @see CustomAsyncTask
 */
public class LocalComicsLoader extends CustomAsyncTask<File,Void,Comics> {

    private OnComicsLoadedListener listener;
    private FailureReason reason = FailureReason.UNKNOWN_LOCAL_ERROR;

    public LocalComicsLoader(OnComicsLoadedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Comics doInBackground(File... files) {
        if(files.length > 0 && files[0] != null && files[0].exists() && files[0].length() > 0) {
            Comics comics = (Comics) Utils.readObjectFromFile(files[0]);
            if(comics != null) {
                ComicsSorter.sort(comics, SortingOption.POPULARITY);
                return comics;
            }
        } else
            reason = FailureReason.NOT_AVAILABLE_LOCALLY;
        return null;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        if(comics != null)
            listener.onComicsLoaded(comics, ComicsDataSource.SourceType.LOCAL_FULL);
        else
            listener.onFailedToLoadComics(reason);
    }
}
