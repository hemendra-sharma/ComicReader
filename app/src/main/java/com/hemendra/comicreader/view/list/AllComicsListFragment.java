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
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.presenter.ComicsPresenter;

import java.util.ArrayList;
import java.util.Locale;

public class AllComicsListFragment extends Fragment {

    private ComicsPresenter comicsPresenter;
    private TextView tvInfo;
    private RecyclerView recycler = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private String[] sorting_options;
    private TextView tvCategoriesSelector = null;

    private CategorySelectionDialog categorySelectionDialog = null;
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

        tvInfo = view.findViewById(R.id.tvInfo);
        recycler = view.findViewById(R.id.recycler);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        Spinner spinnerSorting = view.findViewById(R.id.spinnerSorting);
        tvCategoriesSelector = view.findViewById(R.id.tvCategoriesSelector);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            mAdapter.clearItems();
            mAdapter.notifyDataSetChanged();
            comicsPresenter.invalidateCacheAndLoadComicsAgain();
            swipeRefreshLayout.setRefreshing(false);
            spinnerSorting.setSelection(0);
            categorySelectionDialog.selectAll();
        });

        RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recycler.setLayoutManager(gridLayoutManager);
        recycler.setItemAnimator(new DefaultItemAnimator());

        sorting_options = getResources().getStringArray(R.array.sorting_options);
        spinnerSorting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long l) {
                if(mAdapter != null) {
                    if (sorting_options[position].equals(getString(R.string.popularity))) {
                        comicsPresenter.performSort(mAdapter.getComics(), SortingOption.POPULARITY);
                    } else if (sorting_options[position].equals(getString(R.string.latest_first))) {
                        comicsPresenter.performSort(mAdapter.getComics(), SortingOption.LATEST_FIRST);
                    } else if (sorting_options[position].equals(getString(R.string.a_to_z))) {
                        comicsPresenter.performSort(mAdapter.getComics(), SortingOption.A_TO_Z);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        tvCategoriesSelector.setOnClickListener(v-> {
            if(categorySelectionDialog != null)
                categorySelectionDialog.show();
        });
    }

    public void onComicsLoaded(Comics comics) {
        if(recycler != null) {
            if(mAdapter == null) {
                mAdapter = new ComicsListAdapter(comics, comicsPresenter, listener);
                recycler.setAdapter(mAdapter);
                categorySelectionDialog = new CategorySelectionDialog(getContext(),
                        comics.categories, categorySelectionListener);
                setSelectedCategoriesCount(comics.categories);
            } else {
                mAdapter.setComics(comics);
                mAdapter.notifyDataSetChanged();
            }
            setInfo();
        }
    }

    private CategorySelectionListener categorySelectionListener = selectedCategories -> {
        setSelectedCategoriesCount(selectedCategories);
        comicsPresenter.performFilter(selectedCategories);
    };

    private void setSelectedCategoriesCount(ArrayList<String> selectedCategories) {
        if(tvCategoriesSelector != null) {
            if(selectedCategories.size() == categorySelectionDialog.getTotalCategoriesCount()) {
                tvCategoriesSelector.setText("All");
            } else {
                tvCategoriesSelector.setText(String.format(Locale.getDefault(),
                        "%d Selected", selectedCategories.size()));
            }
        }
    }

    private void setInfo() {
        if(tvInfo != null) {
            tvInfo.setText(String.format(Locale.getDefault(),
                    "Found %d Comics", mAdapter.getItemCount()));
        }
    }

    private OnComicItemClickListener listener = comic -> comicsPresenter.loadComicDetails(comic);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
