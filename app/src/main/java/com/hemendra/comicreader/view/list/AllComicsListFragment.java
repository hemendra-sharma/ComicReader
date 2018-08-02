package com.hemendra.comicreader.view.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.presenter.ComicsPresenter;

public class AllComicsListFragment extends Fragment {

    private ComicsPresenter comicsPresenter;
    private RecyclerView recycler = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;

    private ComicsListAdapter mAdapter = null;

    public static AllComicsListFragment getFragment(ComicsPresenter comicsPresenter) {
        AllComicsListFragment fragment = new AllComicsListFragment();
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
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            mAdapter.clearItems();
            mAdapter.notifyDataSetChanged();
            comicsPresenter.invalidateCacheAndLoadComicsAgain();
            swipeRefreshLayout.setRefreshing(false);
        });

        RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recycler.setLayoutManager(gridLayoutManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
    }

    public void onComicsLoaded(Comics comics) {
        if(recycler != null) {
            if(mAdapter == null) {
                mAdapter = new ComicsListAdapter(comics.comics, comicsPresenter, listener);
                recycler.setAdapter(mAdapter);
            } else {
                mAdapter.setComics(comics.comics);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private OnComicItemClickListener listener = comic -> comicsPresenter.loadComicDetails(comic);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
