package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.list.SortingOption;

import java.util.ArrayList;
import java.util.Collections;

public class ComicsFilterer extends CustomAsyncTask<Void,Void,Comics> {

    private OnComicsLoadedListener listener;
    private Comics comics;
    private ArrayList<String> selectedCategories;
    private SortingOption sortingOption;

    public ComicsFilterer(Comics comics, OnComicsLoadedListener listener,
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
                if (filteredComics.comics.contains(comic))
                    continue;
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
