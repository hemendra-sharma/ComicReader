package com.hemendra.comicreader.view;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.presenter.ComicsPresenter;
import com.hemendra.comicreader.view.list.AllComicsListFragment;

public class ComicsListActivity extends AppCompatActivity implements IComicListActivityCallback {

    private RuntimePermissionManager runtimePermissionManager = null;
    private ComicsPresenter comicsPresenter = null;
    private AllComicsListFragment allComicsListFragment = null;
    private SearchView searchView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_list);
        runtimePermissionManager = new RuntimePermissionManager(this);
        comicsPresenter = ComicsPresenter.getInstance(getApplicationContext(), this);
        allComicsListFragment = AllComicsListFragment.getInstance(comicsPresenter);
        showComicsListFragment();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
            searchView.setOnCloseListener(() -> {
                comicsPresenter.startLoadingComics();
                return false;
            });
        }
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
        comicsPresenter.performSearch(query);
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
    private void showSearchView() {
        searchView.setIconified(true);
        searchView.clearFocus();
        searchView.setVisibility(View.GONE);
    }

    /**
     * Show back the search view on the action bar.
     */
    private void hideSearchView() {
        searchView.setVisibility(View.VISIBLE);
        searchView.setIconified(true);
        searchView.clearFocus();
    }

    private void showComicsListFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.place_holder, allComicsListFragment);
        transaction.commit();
    }

    private void showComicDetailsFragment(Comic comic) {

    }

    private void showComicReaderFragment(Comic comic) {

    }

    @Override
    public void onComicsLoadingStarted() {
        hideSearchView();
    }

    @Override
    public void onComicsLoaded(Comics comics) {
        showSearchView();
        allComicsListFragment.onComicsLoaded(comics);
    }

    @Override
    public void onFailedToLoadComics(String reason) {
        showSearchView();
    }

    @Override
    public void onStoppedLoadingComics() {
        showSearchView();
    }

    @Override
    public void onComicDetailsLoadingStarted() {
        hideSearchView();
    }

    @Override
    public void onComicDetailsLoaded(Comic comic) {

    }

    @Override
    public void onFailedToLoadComicDetails(String reason) {
        showSearchView();
    }

    @Override
    public void onStoppedLoadingComicDetails() {
        showSearchView();
    }

    @Override
    public void onPageDownloaded(String url, Bitmap pageImage) {

    }

    @Override
    public void onFailedToDownloadPage(String url, String reason) {

    }

}
