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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Holds the information about a single comic.
 * @author Hemendra Sharma
 * @see java.io.Serializable
 */
public class Comic implements Serializable {

    /**
     * Keeping the serial version unique identifier, so that we won't loose data when modifying
     * class structure of code.
     */
    private static final long serialVersionUID = 4515267587540133954L;

    /**
     * The unique manga ID obtained from remote API
     */
    public String id;
    /**
     * The comic title to be shown to user.
     */
    public String title;
    /**
     * The ID or Server File Path or the cover image for this comic.
     */
    private String image;
    /**
     * Timestamp of when this comic was last updated.
     */
    public long lastUpdated;
    /**
     * List of categories associated with this comic.
     */
    public ArrayList<String> categories;
    /**
     * Comic summary or description explaining about the comic.
     */
    public String description = "";
    /**
     * Author name of this comic.
     */
    public String author = "";
    /**
     * THe number of how many times this comic book was queried on the server. This number can
     * also represent the popularity of the comic.
     */
    public int hits = 0;
    /**
     * The year when this comic was released.
     */
    public String released = "";
    /**
     * List of chapters inside this comic.
     */
    public ArrayList<Chapter> chapters = new ArrayList<>();
    /**
     * This score represents how good match was a search result when searching with keyword,
     * or filtering with categories. The higher score search results are shown on the top.
     */
    transient public int searchScore = 0;
    /**
     * This flag is set to TRUE if user has marked this comic as favorite.
     */
    public boolean isFavorite = false;

    /**
     * Creates a new instance of {@link Comic}.
     * @param id The unique manga ID obtained from remote API
     * @param title The comic title to be shown to user.
     * @param image The ID or Server File Path or the cover image for this comic.
     * @param lastUpdated Timestamp of when this comic was last updated.
     * @param categories List of categories associated with this comic.
     */
    public Comic(String id, String title, String image,
                 long lastUpdated, ArrayList<String> categories) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.lastUpdated = lastUpdated * 1000L;
        this.categories = categories;
    }

    /**
     * Convert the last-updated timestamp into a human readable date format.
     * @return Date in "MMM dd, yyyy" format.
     */
    public String getLastUpdatedString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(lastUpdated));
    }

    /**
     * A single comma-separated string representation of the list of categories.
     * e.g. Category 1, Category 2, ... Category 'max'
     * @param max The maximum number categories to be concatenated.
     * @return Categories string.
     */
    public String getCategoriesString(int max) {
        StringBuilder cat = new StringBuilder();
        for(int i=0; i<categories.size() && i<max; i++) {
            if(cat.length() > 0)
                cat.append(", ");
            cat.append(categories.get(i));
        }
        if(max < categories.size())
            cat.append("...");
        return cat.toString();
    }

    /**
     * Generate a image url path.
     * @return A valid image URL if the path exists. NULL otherwise.
     */
    @Nullable
    public String getImageUrl() {
        if(image.length() > 0) {
            return RemoteConfig.buildImageUrl(image);
        }
        return null;
    }

    /**
     * Checks whether two comic instances are equal or not, based on their IDs.
     * @param obj Expecting an instance of {@link Comic}
     * @return TRUE if both objects have same IDs. FALSE otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return obj != null
                && obj instanceof Comic
                && ((Comic)obj).id.equals(this.id);
    }

}
