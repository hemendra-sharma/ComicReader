package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;

public class ComicsSearcher extends CustomAsyncTask<String,Void,Comics> {

    private OnComicsLoadedListener listener;
    private Comics comics;

    public ComicsSearcher(Comics comics, OnComicsLoadedListener listener) {
        this.comics = comics;
        this.listener = listener;
    }

    @Override
    protected Comics doInBackground(String... strings) {
        String query = strings[0];
        query = query.trim().toLowerCase();
        Comics filteredComics = new Comics();
        for(Comic comic : comics.comics) {
            if(comic.title.toLowerCase().contains(query)
                    || comic.getCategoriesString(99999).toLowerCase().contains(query)) {
                filteredComics.comics.add(comic);
            }
        }
        return filteredComics;
    }

    @Override
    protected void onPostExecute(Comics comics) {
        listener.onComicsLoaded(comics, ComicsDataSource.SourceType.LOCAL_SEARCH);
    }
}
