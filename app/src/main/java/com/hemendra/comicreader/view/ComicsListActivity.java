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

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.presenter.ComicsPresenter;
import com.hemendra.comicreader.view.details.ComicDetailsFragment;
import com.hemendra.comicreader.view.list.AllComicsListFragment;
import com.hemendra.comicreader.view.reader.ComicReaderFragment;

import static com.hemendra.comicreader.view.MessageBox.showMessage;

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

        comicsPresenter = ComicsPresenter.getInstance(getApplicationContext(), this);

        allComicsListFragment = AllComicsListFragment.getFragment(comicsPresenter);
        comicDetailsFragment = ComicDetailsFragment.getFragment(comicsPresenter);
        comicReaderFragment = ComicReaderFragment.getFragment(comicsPresenter);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            if(getSupportFragmentManager().getBackStackEntryCount() == 2) {
                recoverFromFullScreen();
            } else if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
                showSearchView();
                allComicsListFragment.refreshCurrentView();
            }
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        comicsPresenter.destroy();
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
        searchView.setIconified(true);
        searchView.clearFocus();
        searchView.setVisibility(View.GONE);
    }

    /**
     * Show back the search view on the action bar.
     */
    private void showSearchView() {
        searchView.setVisibility(View.VISIBLE);
        searchView.setIconified(true);
        searchView.clearFocus();
    }

    private void showComicsListFragment() {
        if(allComicsListFragment.isAdded())
            return;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.place_holder, allComicsListFragment)
                .commit();
    }

    private void showComicDetailsFragment(Comic comic) {
        if(comicDetailsFragment.isAdded())
            return;
        comicDetailsFragment.setComic(comic);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.place_holder, comicDetailsFragment)
                .addToBackStack(null)
                .commit();
        hideSearchView();
    }

    private void showComicReaderFragment(Chapter chapter) {
        if(comicReaderFragment.isAdded())
            return;
        makeFullScreen();
        comicReaderFragment.setChapter(chapter);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.place_holder, comicReaderFragment)
                .addToBackStack(null)
                .commit();
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
        rlProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onComicsLoaded(Comics comics) {
        showSearchView();
        rlProgress.setVisibility(View.GONE);
        allComicsListFragment.onComicsLoaded(comics);
    }

    @Override
    public void onFailedToLoadComics(String reason) {
        showSearchView();
        rlProgress.setVisibility(View.GONE);
        showMessage(this, "Failed to Load Comics. Reason: "+reason, null);
    }

    @Override
    public void onComicDetailsLoadingStarted() {
        hideSearchView();
        rlProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onComicDetailsLoaded(Comic comic) {
        rlProgress.setVisibility(View.GONE);
        showComicDetailsFragment(comic);
    }

    @Override
    public void onFailedToLoadComicDetails(String reason) {
        showSearchView();
        rlProgress.setVisibility(View.GONE);
        showMessage(this, "Failed to Load Comic Details. Reason: "+reason, null);
    }

    @Override
    public void onChapterLoadingStarted() {
        hideSearchView();
        rlProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onChapterLoaded(Chapter chapter) {
        rlProgress.setVisibility(View.GONE);
        showComicReaderFragment(chapter);
    }

    @Override
    public void onFailedToLoadChapter(String reason) {
        rlProgress.setVisibility(View.GONE);
        showMessage(this, "Failed to Load Chapter. Reason: "+reason, null);
    }

    @Override
    public void onPageLoaded() {
        comicReaderFragment.refreshFlipView();
    }

}
