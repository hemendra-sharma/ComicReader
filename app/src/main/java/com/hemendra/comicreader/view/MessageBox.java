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

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.hemendra.comicreader.R;

final class MessageBox {

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
