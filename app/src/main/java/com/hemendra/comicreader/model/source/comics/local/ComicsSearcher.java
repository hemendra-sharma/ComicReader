package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.list.SortingOption;

import java.util.ArrayList;
import java.util.Comparator;

public class ComicsSearcher extends CustomAsyncTask<String,Void,Comics> {

    private OnComicsLoadedListener listener;
    private Comics comics;
    private ArrayList<String> selectedCategories;
    private SortingOption sortingOption;

    public ComicsSearcher(Comics comics, OnComicsLoadedListener listener,
                          ArrayList<String> selectedCategories,
                          SortingOption sortingOption) {
        this.comics = comics;
        this.listener = listener;
        this.selectedCategories = selectedCategories;
        this.sortingOption = sortingOption;
    }

    @Override
    protected Comics doInBackground(String... strings) {
        String query = strings[0];
        query = query.trim().toLowerCase();
        while(query.contains("  "))
            query = query.replace("  ", " ");
        String[] parts;
        if(query.contains(" "))
            parts = query.split(" ");
        else
            parts = new String[]{query};
        //
        // search by query
        Comics filteredComics = new Comics();
        for (Comic comic : comics.comics) {
            if (filteredComics.comics.contains(comic))
                continue;
            if(categoryMatch(comic)
                    && comic.title.toLowerCase().contains(query.toLowerCase())) {
                filteredComics.comics.add(comic);
            }
        }
        for(String part : parts) {
            for(Comic comic : comics.comics) {
                if(filteredComics.comics.contains(comic))
                    continue;
                if(categoryMatch(comic)) {
                    if (comic.title.toLowerCase().contains(part)
                            || comic.description.toLowerCase().contains(part)) {
                        filteredComics.comics.add(comic);
                    }
                }
            }
        }
        //
        if(sortingOption == SortingOption.POPULARITY)
            filteredComics.comics.sort((c1, c2) -> Integer.compare(c2.hits, c1.hits));
        else if(sortingOption == SortingOption.LATEST_FIRST)
            filteredComics.comics.sort((c1, c2) -> Long.compare(c2.lastUpdated, c1.lastUpdated));
        else if(sortingOption == SortingOption.A_TO_Z)
            filteredComics.comics.sort(Comparator.comparing(c -> c.title));
        return filteredComics;
    }

    private boolean categoryMatch(Comic comic) {
        for(String category : selectedCategories) {
            if(comic.categories.contains(category))
                return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        listener.onComicsLoaded(comics, ComicsDataSource.SourceType.LOCAL_SEARCH);
    }
}
