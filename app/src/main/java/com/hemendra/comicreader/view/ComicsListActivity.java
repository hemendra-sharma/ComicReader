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

package com.hemendra.comicreader.view;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.presenter.ComicsPresenter;
import com.hemendra.comicreader.view.details.ComicDetailsFragment;
import com.hemendra.comicreader.view.list.AllComicsListFragment;
import com.hemendra.comicreader.view.reader.ComicReaderFragment;

import static com.hemendra.comicreader.view.MessageBox.showMessage;

/**
 * The main activity of the app. It holds all the child fragments.
 * @author Hemendra Sharma
 */
public class ComicsListActivity extends AppCompatActivity implements IComicListActivityCallback {

    private RuntimePermissionManager runtimePermissionManager = null;
    private ComicsPresenter comicsPresenter = null;

    private AllComicsListFragment allComicsListFragment;
    private ComicDetailsFragment comicDetailsFragment;
    private ComicReaderFragment comicReaderFragment;

    private SearchView searchView = null;
    private RelativeLayout rlProgress = null;

    private int savedNavigationBarColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_list);

        rlProgress = findViewById(R.id.rlProgress);

        runtimePermissionManager = new RuntimePermissionManager(this);

        comicsPresenter = new ComicsPresenter(getApplicationContext(), this);

        allComicsListFragment = AllComicsListFragment.getFragment(comicsPresenter);
        comicDetailsFragment = ComicDetailsFragment.getFragment(comicsPresenter);
        comicReaderFragment = ComicReaderFragment.getFragment(comicsPresenter);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            if(isProgressVisible()) {
                Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
                return;
            } else if(getSupportFragmentManager().getBackStackEntryCount() == 2) {
                // currently showing reader
                if(comicReaderFragment.onBackPressed()) return;
                recoverFromFullScreen();
            } else if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
                // currently showing details
                if(comicDetailsFragment.onBackPressed()) return;
                showSearchView();
                allComicsListFragment.refreshCurrentView();
                setTitle(R.string.app_name);
            }
            //
            getSupportFragmentManager().popBackStack();
            refreshChaptersList();
        } else if(!allComicsListFragment.onBackPressed()) {
            // currently showing comics list
            finishAffinity();
        }
    }

    @Override
    public void onDestroy() {
        comicsPresenter.destroy();

        runtimePermissionManager = null;
        comicsPresenter = null;
        allComicsListFragment = null;
        comicDetailsFragment = null;
        comicReaderFragment = null;
        searchView = null;
        rlProgress = null;

        super.onDestroy();
    }

    /**
     * Implementing the search-view
     * @param menu The menu to inflate
     * @return TRUE if handled, FALSE otherwise.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        // show the first fragment after loading the search-view
        showComicsListFragment();

        return true;
    }

    /**
     * This method will be called when user has performed a new search
     * @param intent the search Intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * Handle the search Intent's functionality.
     * @param intent the search Intent
     */
    private void handleIntent(@Nullable Intent intent) {
        // check if searching or not
        String query = "";
        if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // use the query to search comics data
            query = intent.getStringExtra(SearchManager.QUERY);
        }
        if(query.length() > 0)
            comicsPresenter.performSearch(query);
        else
            comicsPresenter.startLoadingComics();
    }

    @Override
    public void askForPermissions() {
        runtimePermissionManager.askForPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        runtimePermissionManager.onRequestPermissionsResult(requestCode,
                permissions, grantResults, ()->comicsPresenter.permissionGranted());
    }

    /**
     * Hide the search view on the action bar.
     */
    private void hideSearchView() {
        if(searchView != null) {
            searchView.setIconified(true);
            searchView.clearFocus();
            searchView.setVisibility(View.GONE);
        }
    }

    /**
     * Show back the search view on the action bar.
     */
    private void showSearchView() {
        if(searchView != null) {
            searchView.setVisibility(View.VISIBLE);
            searchView.setIconified(true);
            searchView.clearFocus();
        }
    }

    private synchronized void showComicsListFragment() {
        if(comicsPresenter == null
                || allComicsListFragment == null
                || allComicsListFragment.isAdded())
            return;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.place_holder, allComicsListFragment)
                .commitAllowingStateLoss();
        setTitle(R.string.app_name);
    }

    private synchronized void showComicDetailsFragment(Comic comic) {
        if(comicsPresenter == null
                || comicDetailsFragment == null
                || comicDetailsFragment.isAdded())
            return;
        comicDetailsFragment.setComic(comic);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.place_holder, comicDetailsFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
        hideSearchView();
        setTitle(getString(R.string.details));
    }

    private synchronized void showComicReaderFragment(Chapter chapter) {
        if(comicsPresenter == null
                || comicReaderFragment == null
                || comicReaderFragment.isAdded())
            return;
        makeFullScreen();
        comicReaderFragment.setChapter(chapter);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.place_holder, comicReaderFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
        hideSearchView();
    }

    private void makeFullScreen() {
        // hide action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
        // go full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        savedNavigationBarColor = getWindow().getNavigationBarColor();
        // navigation bar dark color
        getWindow().setNavigationBarColor(Color.BLACK);
    }

    private void recoverFromFullScreen() {
        // show action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.show();
        // no full screen
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // reset navigation bar color
        getWindow().setNavigationBarColor(savedNavigationBarColor);
    }

    @Override
    public void onComicsLoadingStarted() {
        hideSearchView();
        showProgress();
    }

    @Override
    public void onComicsLoaded(Comics comics) {
        showSearchView();
        hideProgress();
        if(allComicsListFragment != null)
            allComicsListFragment.onComicsLoaded(comics);
    }

    @Override
    public void onFailedToLoadComics(String reason) {
        showSearchView();
        hideProgress();
        showMessage(this, getString(R.string.failed_to_load_comics_reason_s, reason), null);
    }

    @Override
    public void onComicDetailsLoadingStarted() {
        hideSearchView();
        showProgress();
    }

    @Override
    public void onComicDetailsLoaded(Comic comic) {
        hideProgress();
        showComicDetailsFragment(comic);
    }

    @Override
    public void onComicUpdated(Comic comic) {
        if(allComicsListFragment != null)
            allComicsListFragment.onComicUpdated(comic);
    }

    @Override
    public void onFailedToLoadComicDetails(String reason) {
        showSearchView();
        hideProgress();
        showMessage(this, getString(R.string.failed_to_load_comic_details_reason_s, reason), null);
    }

    @Override
    public void onChapterLoadingStarted() {
        hideSearchView();
        showProgress();
    }

    @Override
    public void onChapterLoaded(Chapter chapter) {
        hideProgress();
        showComicReaderFragment(chapter);
    }

    @Override
    public void refreshChaptersList() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 1
                && comicDetailsFragment != null) {
            comicDetailsFragment.refreshChaptersList();
        }
    }

    @Override
    public void onFailedToLoadChapter(String reason) {
        hideProgress();
        showMessage(this, getString(R.string.failed_to_load_chapter_reason_s, reason), null);
    }

    @Override
    public void onPageLoaded() {
        if(comicReaderFragment != null)
            comicReaderFragment.refreshFlipView();
    }

    @Override
    public Chapter getNextChapterFromDetailsFragment(Chapter ch) {
        if(comicDetailsFragment == null)
            return null;
        return comicDetailsFragment.getNextChapterFrom(ch);
    }

    private boolean isProgressVisible() {
        return rlProgress != null && rlProgress.getVisibility() == View.VISIBLE;
    }

    @Override
    public void showProgress() {
        if(rlProgress != null)
            rlProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        if(rlProgress != null)
            rlProgress.setVisibility(View.GONE);
    }

}
