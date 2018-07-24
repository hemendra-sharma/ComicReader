package com.hemendra.comicreader.view.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.presenter.ComicsPresenter;

public class AllComicsListFragment extends Fragment implements IComicsListCallback {

    private ComicsPresenter comicsPresenter;

    private static AllComicsListFragment fragment = null;

    public AllComicsListFragment() {
        super();
        comicsPresenter = ComicsPresenter.getInstance(getContext());
        comicsPresenter.setListView(this);
    }

    public static AllComicsListFragment getFragment() {
        if(fragment == null) {
            fragment = new AllComicsListFragment();
        }
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_comics, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

    }

    @Override
    public void onComicsLoadingStarted() {

    }

    @Override
    public void onComicsLoaded(Comics comics) {

    }

    @Override
    public void onFailedToLoadComics(String reason) {

    }

    @Override
    public void onStoppedLoadingComics() {

    }
}
