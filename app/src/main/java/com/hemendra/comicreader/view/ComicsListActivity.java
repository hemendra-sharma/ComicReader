package com.hemendra.comicreader.view;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.presenter.ComicsPresenter;
import com.hemendra.comicreader.view.list.AllComicsListFragment;

public class ComicsListActivity extends AppCompatActivity implements IComicListActivityCallback {

    private ComicsPresenter comicsPresenter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_list);

        comicsPresenter = ComicsPresenter.getInstance(getApplicationContext());
        comicsPresenter.setActivityView(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.place_holder, AllComicsListFragment.getFragment());
        transaction.commit();
    }

    @Override
    public void askForPermissions() {

    }

    @Override
    public void onDestroy() {
        comicsPresenter.destroy();
        super.onDestroy();
    }

}
