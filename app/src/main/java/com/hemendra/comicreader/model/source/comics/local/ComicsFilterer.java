package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.list.SortingOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        for(String category : selectedCategories) {
            for(Comic comic : comics.comics) {
                if(filteredComics.comics.contains(comic))
                    continue;
                if(comic.categories.contains(category)) {
                    filteredComics.comics.add(comic);
                }
            }
        }
        if(sortingOption == SortingOption.POPULARITY)
            Collections.sort(filteredComics.comics, (c1, c2) -> Integer.compare(c2.hits, c1.hits));
        else if(sortingOption == SortingOption.LATEST_FIRST)
            Collections.sort(filteredComics.comics, (c1, c2) -> Long.compare(c2.lastUpdated, c1.lastUpdated));
        else if(sortingOption == SortingOption.A_TO_Z)
            Collections.sort(filteredComics.comics, (c1, c2) -> c1.title.compareTo(c2.title));
        return filteredComics;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        listener.onComicsLoaded(comics, ComicsDataSource.SourceType.LOCAL_FILTER);
    }
}
