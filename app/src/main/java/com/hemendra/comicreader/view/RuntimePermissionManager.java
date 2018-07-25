package com.hemendra.comicreader.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.hemendra.comicreader.R;

import static com.hemendra.comicreader.view.MessageBox.*;

public class RuntimePermissionManager {

    private static final int REQUEST_READ_WRITE_PERMISSION = 1001;

    private AppCompatActivity activity;

    public RuntimePermissionManager(@NonNull AppCompatActivity activity) {
        this.activity = activity;
    }

    public void askForPermissions() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (activity.shouldShowRequestPermissionRationale(permission)) {
            showMessage(activity,
                    activity.getString(R.string.permission_request_rationale),
                    (dialogInterface, i) ->
                            activity.requestPermissions(new String[]{permission},
                                    REQUEST_READ_WRITE_PERMISSION));
        } else {
            activity.requestPermissions(new String[]{permission}, REQUEST_READ_WRITE_PERMISSION);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults,
                                           @NonNull Runnable onPermissionGranted) {
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
                        showMessage(activity,
                                activity.getString(R.string.permission_request_from_app_settings),
                                (dialogInterface, i) -> launchAppSettingsScreen());
                    } else {
                        onPermissionGranted.run();
                    }
                } else {
                    // If request is cancelled, the result arrays are empty.
                    activity.finish();
                }
            }
        }
    }

    private void launchAppSettingsScreen() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        activity.startActivity(intent);
        activity.finish();
    }
}
