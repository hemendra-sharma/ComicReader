package com.hemendra.comicreader.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.hemendra.comicreader.R;

public final class MessageBox {

    /**
     * Show the {@link AlertDialog} with given message and a single 'OK' option.
     * @param activity The activity reference where this dialog needs to be shown
     * @param msg Message to showMessage
     * @param listener The task to execute when 'OK' is clicked by user
     */
    public static void showMessage(@NonNull AppCompatActivity activity,
                                   @NonNull String msg,
                                   @Nullable DialogInterface.OnClickListener listener) {
        if (activity.isDestroyed() || activity.isFinishing()) {
            return;
        }
        //
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, listener).create();
        alertDialog.show();
    }

}
