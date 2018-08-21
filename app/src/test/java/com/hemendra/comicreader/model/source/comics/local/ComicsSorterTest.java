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

package com.hemendra.comicreader.model.source.comics.local;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.view.list.SortingOption;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ComicsSorterTest {

    private Comics comics = new Comics();
    private Comic comic0, comic1, comic2, comic3, comic4;

    @Before
    public void setUp() {
        comic0 = new Comic("0", "100", "", 3, new ArrayList<>());
        comic0.hits = 1000;
        comic1 = new Comic("1", "103", "", 2, new ArrayList<>());
        comic1.hits = 1001;
        comic2 = new Comic("2", "102", "", 0, new ArrayList<>());
        comic2.hits = 1002;
        comic3 = new Comic("3", "101", "", 1, new ArrayList<>());
        comic3.hits = 1003;
        comic4 = new Comic("4", "104", "", 4, new ArrayList<>());
        comic4.hits = 1004;

        comics.comics.add(comic0);
        comics.comics.add(comic1);
        comics.comics.add(comic2);
        comics.comics.add(comic3);
        comics.comics.add(comic4);
    }

    @Test
    public void sortByPopularity() {
        // sort by popularity
        Comics popularitySorted = new Comics();
        popularitySorted.comics.add(comic4);
        popularitySorted.comics.add(comic3);
        popularitySorted.comics.add(comic2);
        popularitySorted.comics.add(comic1);
        popularitySorted.comics.add(comic0);
        ComicsSorter.sort(comics, SortingOption.POPULARITY);
        assertArrayEquals(comics.comics.toArray(), popularitySorted.comics.toArray());
    }

    @Test
    public void sortByLatestFirst() {
        // sort by latest
        Comics latestSorted = new Comics();
        latestSorted.comics.add(comic4);
        latestSorted.comics.add(comic0);
        latestSorted.comics.add(comic1);
        latestSorted.comics.add(comic3);
        latestSorted.comics.add(comic2);
        ComicsSorter.sort(comics, SortingOption.LATEST_FIRST);
        assertArrayEquals(comics.comics.toArray(), latestSorted.comics.toArray());
    }

    @Test
    public void sortByA_to_Z() {
        // sort by latest
        Comics aToZSorted = new Comics();
        aToZSorted.comics.add(comic0);
        aToZSorted.comics.add(comic3);
        aToZSorted.comics.add(comic2);
        aToZSorted.comics.add(comic1);
        aToZSorted.comics.add(comic4);
        ComicsSorter.sort(comics, SortingOption.A_TO_Z);
        assertArrayEquals(comics.comics.toArray(), aToZSorted.comics.toArray());
    }

    @After
    public void tearDown() {
        comics.comics.clear();
        comics = null;
        comic0 = null;
        comic1 = null;
        comic2 = null;
        comic3 = null;
        comic4 = null;
    }
}