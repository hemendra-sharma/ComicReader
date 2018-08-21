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

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.Assert.*;

public class ComicTest {

    @Test
    public void getLastUpdatedString() throws ParseException {
        Comic comic = new Comic("123", "Title-1", "", System.currentTimeMillis(),
                new ArrayList<>());
        String dateString = comic.getLastUpdatedString();
        new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(dateString);
    }

    @Test
    public void getCategoriesString() {
        Comic comic = new Comic("123", "Title-1", "", System.currentTimeMillis(),
                new ArrayList<>());
        comic.categories.add("Action");
        comic.categories.add("Horror");
        comic.categories.add("Drama");
        comic.categories.add("Sci-fi");

        assertEquals("Incorrect Category String for max=1",
                comic.getCategoriesString(1), "Action...");

        assertEquals("Incorrect Category String for max=2",
                comic.getCategoriesString(2), "Action, Horror...");

        assertEquals("Incorrect Category String for max=3",
                comic.getCategoriesString(3), "Action, Horror, Drama...");

        assertEquals("Incorrect Category String for max=4",
                comic.getCategoriesString(4), "Action, Horror, Drama, Sci-fi");

        assertEquals("Incorrect Category String for max > array-length",
                comic.getCategoriesString(5), "Action, Horror, Drama, Sci-fi");
    }

    @Test
    public void getImageUrl() {
        Comic comic = new Comic("123", "Title-1", "", System.currentTimeMillis(),
                new ArrayList<>());

        assertEquals("It should have returned NULL", null, comic.getImageUrl());

        Comic comic1 = new Comic("123", "Title-1", "123456", System.currentTimeMillis(),
                new ArrayList<>());
        String url = comic1.getImageUrl();
        assert url != null && url.length() > 0;
    }

    @Test
    public void equals() {
        Comic comic1 = new Comic("123", "Title", "", System.currentTimeMillis(), new ArrayList<>());
        Comic comic2 = new Comic("123", "Title", "", System.currentTimeMillis(), new ArrayList<>());

        assertTrue("Returned FALSE where comics have same ID", comic1.equals(comic2));

        Comic comic3 = new Comic("234", "Title", "", System.currentTimeMillis(), new ArrayList<>());
        Comic comic4 = new Comic("748", "Title", "", System.currentTimeMillis(), new ArrayList<>());

        assertFalse("Returned TRUE where comics have different ID", comic3.equals(comic4));

        Comic comic5 = new Comic("123", "Title", "", System.currentTimeMillis(), new ArrayList<>());
        Comic comic6 = new Comic("123 ", "Title", "", System.currentTimeMillis(), new ArrayList<>());

        assertFalse("Returned TRUE where comics have different ID", comic5.equals(comic6));
    }
}