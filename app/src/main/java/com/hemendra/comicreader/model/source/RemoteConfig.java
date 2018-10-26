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

package com.hemendra.comicreader.model.source;

import android.support.annotation.NonNull;

import java.net.URLEncoder;

/**
 * Builds the URLs for downloading comics, chapters, and pages.
 */
public final class RemoteConfig {

    private static final String COMICS_LIST_URL = "https://www.mangaeden.com/api/list/0/";
    private static final String COMIC_DETAILS_URL = "https://www.mangaeden.com/api/manga/";
    private static final String CHAPTER_URL = "https://www.mangaeden.com/api/chapter/";
    private static final String IMAGE_URL = "https://cdn.mangaeden.com/mangasimg/";
    private static final String RESIZING_IMAGE_URL
            = "https://images1-focus-opensocial.googleusercontent.com/gadgets/proxy?container=focus";

    /**
     * Returns the URL for downloading the list of comics.
     */
    @NonNull
    public static String buildComicsUrl() {
        return COMICS_LIST_URL;
    }

    /**
     * Returns the URL to load comics details and list of chapters.
     * @param comic_id The ID of comic the load
     */
    @NonNull
    public static String buildComicDetailsUrl(@NonNull String comic_id) {
        return COMIC_DETAILS_URL +
                comic_id + "/";
    }

    /**
     * Returns the URL the load the list of pages.
     * @param chapter_id The ID of chapter to load
     */
    @NonNull
    public static String buildChapterUrl(@NonNull String chapter_id) {
        return CHAPTER_URL +
                chapter_id + "/";
    }

    /**
     * Returns the URL to load the image.
     * @param image_path The server-image path to load.
     */
    @NonNull
    public static String buildImageUrl(@NonNull String image_path) {
        return IMAGE_URL +
                image_path;
    }

    @NonNull
    public static String buildResizingImageUrl(@NonNull String image_path, int width, int height) {
        return RESIZING_IMAGE_URL +
                "&url=" + URLEncoder.encode(buildImageUrl(image_path)) +
                "&resize_w=" + width +
                "&resize_h=" + height;
    }

}
