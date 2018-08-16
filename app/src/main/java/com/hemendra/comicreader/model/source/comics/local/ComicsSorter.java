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
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.list.SortingOption;

import java.util.Collections;

/**
 * A background worker thread to sort the comics based on the selected sorting type.
 * @author Hemendra Sharma
 * @see CustomAsyncTask
 */
public class ComicsSorter extends CustomAsyncTask<Comics,Void,Comics> {

    private OnComicsLoadedListener listener;
    private SortingOption option;

    public ComicsSorter(OnComicsLoadedListener listener, SortingOption option) {
        this.listener = listener;
        this.option = option;
    }

    @Override
    protected Comics doInBackground(Comics... params) {
        Comics comics = params[0];
        sort(comics, option);
        return comics;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        listener.onComicsLoaded(comics, ComicsDataSource.SourceType.LOCAL_SORT);
    }

    public static void sort(Comics comics, SortingOption option) {
        if(option == SortingOption.POPULARITY)
            Collections.sort(comics.comics, (c1, c2) -> Integer.compare(c2.hits, c1.hits));
        else if(option == SortingOption.LATEST_FIRST)
            Collections.sort(comics.comics, (c1, c2) -> Long.compare(c2.lastUpdated, c1.lastUpdated));
        else if(option == SortingOption.A_TO_Z)
            Collections.sort(comics.comics, (c1, c2) -> c1.title.compareTo(c2.title));
    }
}
