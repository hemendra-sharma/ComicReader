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

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.hemendra.comicreader.view.reader.TouchImageView;

/**
 * Provides encapsulation for the Image-View and Bitmap.
 */
public class ImageAndViewHolder {

    private View view;
    private Bitmap bmp = null;

    public ImageAndViewHolder(View view) {
        this.view = view;
    }

    public void setImage(Bitmap bmp) {
        if(bmp != null) {
            this.bmp = bmp;
            if(view instanceof TouchImageView) {
                ((TouchImageView) view).setImageBitmap(bmp);
                view.setTag(-1);
            } else if(view instanceof ImageView) {
                ((ImageView) view).setImageBitmap(bmp);
            }
        }
    }

    public Bitmap getBitmap() {
        return bmp;
    }

    public boolean isCover() {
        return view instanceof ImageView
                && !isPage();
    }

    public boolean isPage() {
        return view instanceof TouchImageView;
    }
}
