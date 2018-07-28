package com.hemendra.comicreader.model.source;

import android.support.annotation.NonNull;

public final class RemoteConfig {

    private static final String COMICS_LIST_URL = "https://www.mangaeden.com/api/list/0/";
    private static final String COMIC_DETAILS_URL = "https://www.mangaeden.com/api/manga/";
    private static final String IMAGE_URL = "https://cdn.mangaeden.com/mangasimg/";

    public static final int COMICS_PER_PAGE = 500;

    @NonNull
    public static String buildComicsUrl(int page_number) {
        StringBuilder url = new StringBuilder();
        url.append(COMICS_LIST_URL);
        if(page_number >= 0) {
            url.append("?p=").append(page_number);
        }
        return url.toString();
    }

    @NonNull
    public static String buildComicDetailsUrl(@NonNull String comic_id) {
        return COMIC_DETAILS_URL +
                comic_id + "/";
    }

    @NonNull
    public static String buildImageUrl(@NonNull String image_path) {
        return IMAGE_URL +
                image_path;
    }

}
