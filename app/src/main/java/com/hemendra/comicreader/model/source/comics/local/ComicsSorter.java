package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.view.list.SortingOption;

import java.util.Collections;

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
            Collections.sort(comics.comics, (c1, c2) -> Integer.compare(c2.hits, c1.hits));
        else if(option == SortingOption.LATEST_FIRST)
            Collections.sort(comics.comics, (c1, c2) -> Long.compare(c2.lastUpdated, c1.lastUpdated));
        else if(option == SortingOption.A_TO_Z)
            Collections.sort(comics.comics, (c1, c2) -> c1.title.compareTo(c2.title));
        return comics;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        listener.onComicsLoaded(comics, ComicsDataSource.SourceType.LOCAL_SORT);
    }
}
