package com.hemendra.comicreader.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.presenter.ComicsPresenter;
import com.hemendra.comicreader.view.list.AllComicsListFragment;

public class ComicsListActivity extends AppCompatActivity implements IComicListActivityCallback {

    private static final int REQUEST_READ_WRITE_PERMISSION = 1001;

    private ComicsPresenter comicsPresenter = null;

    private AllComicsListFragment allComicsListFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_list);

        comicsPresenter = ComicsPresenter.getInstance(getApplicationContext(), this);

        allComicsListFragment = AllComicsListFragment.getInstance(comicsPresenter);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.place_holder, allComicsListFragment);
        transaction.commit();
    }

    @Override
    public void askForPermissions() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (shouldShowRequestPermissionRationale(permission)) {
            showMessageBox(getString(R.string.permission_request_rationale),
                    (dialogInterface, i) ->
                            requestPermissions(new String[]{permission},
                                    REQUEST_READ_WRITE_PERMISSION));
        } else {
            requestPermissions(new String[]{permission}, REQUEST_READ_WRITE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_WRITE_PERMISSION: {
                if (permissions.length > 0 && grantResults.length > 0) {
                    boolean all_permissions_granted = true;
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            all_permissions_granted = false;
                        }
                    }
                    //
                    if (!all_permissions_granted) {
                        showMessageBox(getString(R.string.permission_request_from_app_settings),
                                (dialogInterface, i) -> {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                    finish();
                                });
                    } else {
                        comicsPresenter.permissionGranted();
                    }
                } else {
                    // If request is cancelled, the result arrays are empty.
                    finish();
                }
            }
        }
    }

    /**
     * Show the {@link AlertDialog} with given message and a single 'OK' option.
     * @param msg Message to show
     * @param listener The task to execute when 'OK' is clicked by user
     */
    private void showMessageBox(@NonNull String msg,
                                @Nullable DialogInterface.OnClickListener listener) {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        //
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, listener).create();
        alertDialog.show();
    }

    @Override
    public void onComicsLoadingStarted() {

    }

    @Override
    public void onComicsLoaded(Comics comics) {
        allComicsListFragment.onComicsLoaded(comics);
    }

    @Override
    public void onFailedToLoadComics(String reason) {

    }

    @Override
    public void onStoppedLoadingComics() {

    }

    @Override
    public void onComicDetailsLoadingStarted() {

    }

    @Override
    public void onComicDetailsLoaded(Comic comic) {

    }

    @Override
    public void onFailedToLoadComicDetails(String reason) {

    }

    @Override
    public void onStoppedLoadingComicDetails() {

    }

    @Override
    public void onPageDownloaded(String url, Bitmap pageImage) {

    }

    @Override
    public void onFailedToDownloadPage(String url, String reason) {

    }

    @Override
    public void onDestroy() {
        comicsPresenter.destroy();
        super.onDestroy();
    }

}
