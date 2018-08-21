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

import android.annotation.SuppressLint;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.view.list.SortingOption;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ComicsSearcherTest {

    private Comics comics = new Comics();
    private String[] comicTitles = new String[]{
            "Dragon Ball",
            "Death Note",
            "One Piece",
            "Game of Thrones",
            "A very very long title which can be searched",
            "The Note",
            "The Dance of the Dragons"
    };

    private ArrayList[] categoriesList = new ArrayList[]{
            new ArrayList<String>(){{ add("Action"); add("Drama"); }},
            new ArrayList<String>(){{ add("Adventure"); add("Drama"); }},
            new ArrayList<String>(){{ add("Horror"); }},
            new ArrayList<String>(){{ add("Sci-fi"); }},
            new ArrayList<String>(){{ add("Action"); add("Romance"); }},
            new ArrayList<String>(){{ add("Horror"); add("Drama"); }},
            new ArrayList<String>(){{ add("Action"); add("Sci-fi"); }}
    };

    @SuppressLint("Unchecked")
    @Before
    public void setUp() {
        for(int i=0; i<comicTitles.length; i++) {
            Comic comic = new Comic(String.valueOf(i), comicTitles[i],
                    "", (i+1)*100, categoriesList[i]);
            comic.hits = 10000 - ((i+1)*10); // descending popularity
            comics.comics.add(comic);
        }
    }

    @Test
    public void searchEmpty() {
        ComicsSearcher searcher = new ComicsSearcher(comics, null,
                new ArrayList<>(), SortingOption.POPULARITY);
        Comics filteredComics = searcher.getFilteredComics("");

        assertArrayEquals(comics.comics.toArray(), filteredComics.comics.toArray());
    }

    @Test
    public void searchAndSortByPopularityWithoutCategories() {
        ComicsSearcher searcher = new ComicsSearcher(comics, null,
                new ArrayList<>(), SortingOption.POPULARITY);
        Comics filteredComics = searcher.getFilteredComics("dragon");

        Comics expectedResults = new Comics();
        expectedResults.comics.add(new Comic("0", comicTitles[0], "", 0, new ArrayList<>()));
        expectedResults.comics.add(new Comic("6", comicTitles[6], "", 0, new ArrayList<>()));

        assertArrayEquals(expectedResults.comics.toArray(), filteredComics.comics.toArray());
    }

    @Test
    public void searchAndSortByPopularity() {
        ArrayList<String> categories = new ArrayList<String>(){{
            add("Adventure");
        }};
        ComicsSearcher searcher = new ComicsSearcher(comics, null,
                categories, SortingOption.POPULARITY);
        Comics filteredComics = searcher.getFilteredComics("note");

        Comics expectedResults = new Comics();
        expectedResults.comics.add(new Comic("1", comicTitles[1], "", 0, new ArrayList<>()));

        assertArrayEquals(expectedResults.comics.toArray(), filteredComics.comics.toArray());
    }

    @After
    public void tearDown() {
        comics.comics.clear();
        comics = null;
    }
}