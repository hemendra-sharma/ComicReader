package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.list.SortingOption;

import java.util.Comparator;

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
        if(option == SortingOption.POPULARITY)
            comics.comics.sort((c1, c2) -> Integer.compare(c2.hits, c1.hits));
        else if(option == SortingOption.LATEST_FIRST)
            comics.comics.sort((c1, c2) -> Long.compare(c2.lastUpdated, c1.lastUpdated));
        else if(option == SortingOption.A_TO_Z)
            comics.comics.sort(Comparator.comparing(c -> c.title));
        return comics;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        listener.onComicsLoaded(comics, ComicsDataSource.SourceType.LOCAL_SORT);
    }
}
