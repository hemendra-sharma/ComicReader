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

public class AllComicsListFragment extends Fragment {

    private ComicsPresenter comicsPresenter;

    private static AllComicsListFragment fragment = null;

    public static AllComicsListFragment getInstance(ComicsPresenter comicsPresenter) {
        if(fragment == null) {
            fragment = new AllComicsListFragment();
        }
        fragment.comicsPresenter = comicsPresenter;
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onComicsLoaded(Comics comics) {

    }
}
