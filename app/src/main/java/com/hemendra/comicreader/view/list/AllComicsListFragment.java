/*
 * Copyright (c) 2018 Hemendra Sharma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hemendra.comicreader.view.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.presenter.ComicsPresenter;

import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class AllComicsListFragment extends Fragment {

    private ComicsPresenter comicsPresenter;
    private TextView tvInfo;
    private RecyclerView recycler = null;
    private String[] sorting_options;
    private TextView tvCategoriesSelector = null;
    private CheckBox cbFavoritesOnly;

    private CardView cardOptions;

    private boolean isTutorialShowing = false;
    private static final String TUTORIAL_ID = "list_tutorial";

    private CategorySelectionDialog categorySelectionDialog = null;
    private AllComicsListAdapter mAdapter = null;

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
        Spinner spinnerSorting = view.findViewById(R.id.spinnerSorting);
        tvCategoriesSelector = view.findViewById(R.id.tvCategoriesSelector);
        cbFavoritesOnly = view.findViewById(R.id.cbFavoritesOnly);
        cardOptions = view.findViewById(R.id.cardOptions);

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

        cbFavoritesOnly.setOnCheckedChangeListener((b, checked) -> {
            if(mAdapter != null) {
                if (checked)
                    mAdapter.setType(AllComicsListAdapter.TYPE_FAVORITES);
                else
                    mAdapter.setType(AllComicsListAdapter.TYPE_ALL);
                mAdapter.notifyDataSetChanged();
            }
        });

        tvInfo.setText(getString(R.string.found_d_comics, 0));
        tvCategoriesSelector.setText(getString(R.string._d_selected, 0));
    }

    public boolean onBackPressed() {
        return isTutorialShowing;
    }

    public void refreshCurrentView() {
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void onComicUpdated(Comic comic) {
        if(recycler != null && mAdapter != null) {
            mAdapter.updateComic(comic);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void onComicsLoaded(Comics comics) {
        if(recycler != null) {
            if(mAdapter == null) {
                mAdapter = new AllComicsListAdapter(comics, comicsPresenter, listener);
                recycler.setAdapter(mAdapter);
                categorySelectionDialog = new CategorySelectionDialog(getContext(),
                        comics.categories, categorySelectionListener);
                setSelectedCategoriesCount(comics.categories);
                if(mAdapter.hasFavorites()) {
                    cbFavoritesOnly.setChecked(true);
                }
            } else {
                mAdapter.setComics(comics);
                mAdapter.notifyDataSetChanged();
            }
            recycler.smoothScrollToPosition(0);
            setInfo();
            showTutorial();
        }
    }

    private CategorySelectionListener categorySelectionListener = selectedCategories -> {
        setSelectedCategoriesCount(selectedCategories);
        comicsPresenter.performFilter(selectedCategories);
    };

    private void setSelectedCategoriesCount(ArrayList<String> selectedCategories) {
        if(tvCategoriesSelector != null && categorySelectionDialog != null) {
            if(selectedCategories.size() == categorySelectionDialog.getTotalCategoriesCount()) {
                tvCategoriesSelector.setText(R.string.all);
            } else {
                tvCategoriesSelector.setText(
                        getString(R.string._d_selected, selectedCategories.size()));
            }
        }
    }

    private void setInfo() {
        if(tvInfo != null && mAdapter != null) {
            tvInfo.setText(getString(R.string.found_d_comics, mAdapter.getItemCount()));
        }
    }

    private OnComicItemClickListener listener = comic -> comicsPresenter.loadComicDetails(comic);

    private void showTutorial() {
        if(getActivity() == null)
            return;
        new MaterialShowcaseView.Builder(getActivity())
                .setTarget(cardOptions)
                .setDismissText(getString(R.string.got_it))
                .setContentText(R.string.you_can_filter_the_comics_using_these_options)
                .setDelay(500)
                .singleUse(TUTORIAL_ID)
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {
                        isTutorialShowing = true;
                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                        isTutorialShowing = false;
                    }
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        mAdapter = null;
        super.onDestroyView();
    }

}
