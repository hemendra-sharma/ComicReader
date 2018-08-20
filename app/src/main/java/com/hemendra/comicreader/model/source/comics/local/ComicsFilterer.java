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

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.list.SortingOption;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A background worker thread to filter the comics based on the selected categories.
 * The best matches show up on the top of the search results.
 * @author Hemendra Sharma
 * @see CustomAsyncTask
 */
public class ComicsFilterer extends CustomAsyncTask<Void,Void,Comics> {

    private OnComicsLoadedListener listener;
    private Comics comics;
    private ArrayList<String> selectedCategories;
    private SortingOption sortingOption;

    ComicsFilterer(Comics comics, OnComicsLoadedListener listener,
                   ArrayList<String> selectedCategories, SortingOption sortingOption) {
        this.comics = comics;
        this.listener = listener;
        this.selectedCategories = selectedCategories;
        this.sortingOption = sortingOption;
    }

    @Override
    protected Comics doInBackground(Void... params) {
        Comics filteredComics = new Comics();
        if(selectedCategories.size() == comics.categories.size()) {
            // selected all... return all
            filteredComics = comics;
        } else {
            for (Comic comic : comics.comics) {
                int score = 0;
                for (String category : selectedCategories) {
                    if (comic.categories.contains(category)) {
                        score++;
                    }
                }
                if(score > 0) {
                    comic.searchScore = score;
                    filteredComics.comics.add(comic);
                }
            }
        }
        ComicsSorter.sort(filteredComics, sortingOption);
        Collections.sort(filteredComics.comics, (c1, c2) -> Integer.compare(c2.searchScore, c1.searchScore));
        return filteredComics;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        listener.onComicsLoaded(comics, ComicsDataSource.SourceType.LOCAL_FILTER);
    }
}
