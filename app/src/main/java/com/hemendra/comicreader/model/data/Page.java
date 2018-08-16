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

package com.hemendra.comicreader.model.data;

import android.support.annotation.Nullable;

import com.hemendra.comicreader.model.source.RemoteConfig;

import java.io.Serializable;

/**
 * Holds the information about a single page of chapter.
 * @author Hemendra Sharma
 * @see java.io.Serializable
 */
public class Page implements Serializable {

    /**
     * Keeping the serial version unique identifier, so that we won't loose data when modifying
     * class structure of code.
     */
    private static final long serialVersionUID = -5997692223648746465L;
    /**
     * The page sequence number. This is used to arraneg all the pages in the ascending order.
     */
    public int number;
    /**
     * The unique page ID obtained from remote API
     */
    private String id;
    /**
     * The raw page image data downloaded as byte array
     */
    public byte[] rawImageData = null;

    /**
     * Creates a new instance of {@link Page}
     * @param number The page sequence number. This is used to arraneg all the pages in the ascending order.
     * @param id The raw page image data downloaded as byte array
     */
    public Page(int number, String id) {
        this.number = number;
        this.id = id;
    }

    /**
     * Create a copy of this page instance excluding the raw byte array page image data.
     * @return A new instance of {@link Page} with same data as this instance.
     */
    public Page getCopyWithoutRawImageData() {
        return new Page(number, id);
    }

    /**
     * Generate a image url path.
     * @return A valid image URL if the path exists. NULL otherwise.
     */
    @Nullable
    public String getImageUrl() {
        if(id.length() > 0) {
            return RemoteConfig.buildImageUrl(id);
        }
        return null;
    }
}
