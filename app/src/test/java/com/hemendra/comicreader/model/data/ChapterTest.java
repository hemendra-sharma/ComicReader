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
import java.util.Locale;

import static org.junit.Assert.*;

public class ChapterTest {

    @Test
    public void getLastUpdatedString() throws ParseException {
        Chapter chapter = new Chapter("123", 0, "Title-1", System.currentTimeMillis());
        String dateString = chapter.getLastUpdatedString();
        new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(dateString);
    }

    @Test
    public void getCopyWithoutRawPageData() {
        Chapter chapter = new Chapter("384923", 312, "Title-1", 73823L);

        Page page1 = new Page(0, "0");
        page1.rawImageData = new byte[10];
        chapter.pages.add(page1);

        Chapter chapterCopy = chapter.getCopyWithoutRawPageData();

        assertEquals("Page Data Missing", 1, chapterCopy.pages.size());
        assertEquals("Copy Contains Image Data", null, chapterCopy.pages.get(0).rawImageData);
    }

    @Test
    public void equals() {
        Chapter chapter1 = new Chapter("123", 0, "ABC", 9999L);
        Chapter chapter2 = new Chapter("123", 1, "XYZ", 8888L);

        assertTrue("Returned FALSE where chapters have same ID", chapter1.equals(chapter2));

        Chapter chapter3 = new Chapter("234", 22, "Test1", 3489L);
        Chapter chapter4 = new Chapter("748", 43, "Test2", 8549L);

        assertFalse("Returned TRUE where chapters have different ID", chapter3.equals(chapter4));

        Chapter chapter5 = new Chapter("123", 0, "ABC ", 9999L);
        Chapter chapter6 = new Chapter("123 ", 1, "XYZ", 8888L);

        assertFalse("Returned TRUE where chapters have different ID", chapter5.equals(chapter6));
    }
}