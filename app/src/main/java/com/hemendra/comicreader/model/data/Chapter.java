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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Holds the information about a single chapter of comic.
 * @author Hemendra Sharma
 * @see java.io.Serializable
 */
public class Chapter implements Serializable {

    /**
     * Keeping the serial version unique identifier, so that we won't loose data when modifying
     * class structure of code.
     */
    private static final long serialVersionUID = -11016580311731648L;

    /**
     * The unique chapter ID obtained from remote API
     */
    public String id;
    /**
     * The chapter sequence number. This is used to arraneg all the chapters in the ascending order.
     */
    public int number;
    /**
     * The chapter title to be shown to user.
     */
    public String title;
    /**
     * The timestamp when this chapter was last updated.
     */
    private long dateUpdated;
    /**
     * List of pages inside this chapter.
     */
    public ArrayList<Page> pages = new ArrayList<>();
    /**
     * Store the information about how many pages user have read.
     */
    public int readingProgress = 0;

    /**
     * Create a new instance of {@link Chapter}
     * @param id The unique chapter ID obtained from remote API
     * @param number The chapter sequence number. This is used to arraneg all the chapters in the ascending order.
     * @param title The chapter title to be shown to user.
     * @param dateUpdated The UNIX timestamp when this chapter was last updated.
     */
    public Chapter(String id, int number, String title, long dateUpdated) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.dateUpdated = dateUpdated * 1000L;
    }

    /**
     * Convert the last-updated timestamp into a human readable date format.
     * @return Date in "MMM dd, yyyy" format.
     */
    public String getLastUpdatedString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(dateUpdated));
    }

    /**
     * Create a copy of this chapter instance excluding the raw byte array page data.
     * @return A new instance of {@link Chapter} with same data as this instance.
     */
    public Chapter getCopyWithoutRawPageData() {
        Chapter chapter = new Chapter(id, number, title, dateUpdated);
        chapter.readingProgress = readingProgress;
        for(Page page : pages) {
            chapter.pages.add(page.getCopyWithoutRawImageData());
        }
        return chapter;
    }

    /**
     * Checks whether two chapter instances are equal or not, based on their IDs.
     * @param obj Expecting an instance of {@link Chapter}
     * @return TRUE if both objects have same IDs. FALSE otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return obj != null
                && obj instanceof Chapter
                && ((Chapter)obj).id.equals(id);
    }
}
