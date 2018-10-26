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

import static org.junit.Assert.*;

public class PageTest {

    @Test
    public void getCopyWithoutRawImageData() {
        Page page = new Page(0, "0", 0, 0);
        page.rawImageData = new byte[10];

        Page pageCopy = page.getCopyWithoutRawImageData();
        assertEquals("Copy Contains Image Data", null, pageCopy.rawImageData);
    }

    @Test
    public void getImageUrl() {
        Page page1 = new Page(232, "", 0, 0);
        assertEquals("Page URL must be NULL at this point", null, page1.getImageUrl());

        Page page2 = new Page(232, "123", 0, 0);
        String url = page2.getImageUrl();
        assert url != null && url.length() > 0;
    }
}