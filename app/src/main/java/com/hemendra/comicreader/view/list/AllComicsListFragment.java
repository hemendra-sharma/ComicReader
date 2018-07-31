package com.hemendra.comicreader.view.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.presenter.ComicsPresenter;

public class AllComicsListFragment extends Fragment {

    private ComicsPresenter comicsPresenter;
    private RecyclerView recycler = null;

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
        comicsPresenter.startLoadingComics();
        recycler = view.findViewById(R.id.recycler);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onComicsLoaded(Comics comics) {
        if(recycler != null) {
            ComicsListAdapter mAdapter = new ComicsListAdapter(comics.comics);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            recycler.setLayoutManager(mLayoutManager);
            recycler.setItemAnimator(new DefaultItemAnimator());
            recycler.setAdapter(mAdapter);
        }
    }
}
