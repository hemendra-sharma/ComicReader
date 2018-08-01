package com.hemendra.comicreader.model.source.comics.remote;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ComicsParser {

    @Nullable
    public static Comics parseComicsFromJSON(@NonNull String json) {
        try {
            if(new JSONTokener(json).nextValue() instanceof JSONObject) {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has("manga")) {
                    if(new JSONTokener(jsonObject.getString("manga")).nextValue()
                            instanceof JSONArray) {
                        Comics comics = new Comics();
                        JSONArray mangaArray = jsonObject.getJSONArray("manga");
                        int count = mangaArray.length();
                        for(int i=0; i<count; i++) {
                            if(new JSONTokener(mangaArray.getString(i)).nextValue()
                                    instanceof JSONObject) {
                                JSONObject mangaObject = mangaArray.getJSONObject(i);

                                String id = mangaObject.optString("i");
                                String title = mangaObject.optString("t");
                                String image = mangaObject.optString("im");
                                long lastUpdated = (long) mangaObject.optDouble("ld",
                                        System.currentTimeMillis());

                                ArrayList<String> categories = new ArrayList<>();
                                if(mangaObject.has("c")) {
                                    if (new JSONTokener(mangaObject.getString("c")).nextValue()
                                            instanceof JSONArray) {
                                        JSONArray categoriesArray = mangaObject.getJSONArray("c");
                                        int len = categoriesArray.length();
                                        for (int x = 0; x < len; x++) {
                                            categories.add(categoriesArray.getString(x));
                                        }
                                    }
                                }

                                title = title.equalsIgnoreCase("null") ? "-" : title;
                                image = image.equalsIgnoreCase("null") ? "" : image;

                                Comic comic = new Comic(id, title, image, lastUpdated, categories);
                                comics.comics.add(comic);
                            }
                        }
                        return comics;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static Comic parseChaptersFromJSON(@NonNull Comic comic, @NonNull String json) {
        try {
            if(new JSONTokener(json).nextValue() instanceof JSONObject) {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has("chapters")
                        && new JSONTokener(jsonObject.getString("chapters")).nextValue() instanceof JSONArray) {
                    JSONArray chaptersArray = jsonObject.getJSONArray("chapters");
                    int count = chaptersArray.length();
                    comic.chapters.clear();
                    for(int i=0; i<count; i++) {
                        if(new JSONTokener(chaptersArray.getString(i)).nextValue()
                                instanceof JSONArray) {
                            JSONArray arr = chaptersArray.getJSONArray(i);
                            if(arr.length() >= 4) {
                                int number = arr.getInt(0);
                                long dateUpdated = (long) arr.getDouble(1);
                                String title = arr.getString(2);
                                title = title.equalsIgnoreCase("null") ? "-" : title;
                                String id = arr.getString(3);
                                comic.chapters.add(new Chapter(id, number, title, dateUpdated));
                            }
                        }
                    }
                }

                if(jsonObject.has("description")) {
                    comic.description = jsonObject.getString("description");
                }

                if(jsonObject.has("author")) {
                    comic.author = jsonObject.getString("author");
                }

                if(jsonObject.has("hits")) {
                    comic.hits = jsonObject.getString("hits");
                }

                if(jsonObject.has("released")) {
                    comic.released = jsonObject.getString("released");
                }

                comic.chapters.sort(Comparator.comparingInt(ch -> ch.number));

                return comic;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
