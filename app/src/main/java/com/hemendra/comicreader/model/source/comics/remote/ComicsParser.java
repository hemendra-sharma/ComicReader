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

package com.hemendra.comicreader.model.source.comics.remote;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.JsonToken;

import com.crashlytics.android.Crashlytics;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.data.Page;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Parses the list of comics, chapters, or pages from JSON.
 * @author Hemendra Sharma
 */
public class ComicsParser {

    /**
     * Parses the list of comics from the given {@link InputStream}. It does not consume any
     * additional memory, and directly converts the stream into object.
     * @param in The input stream reading the response from server.
     * @return The instance of {@link Comics} if stream was parse successfully. NULL otherwise.
     */
    @Nullable
    public static Comics parseComicsFromJSON(@NonNull InputStream in) {
        try{
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            reader.beginObject();
            Comics comics = new Comics();
            ArrayList<String> allCategories = new ArrayList<>();
            while(reader.hasNext()) {
                String name = reader.nextName();
                if(name.equals("manga")) {
                    reader.beginArray();
                    while(reader.hasNext()) {
                        reader.beginObject();
                        String id = "", title = "", image = "";
                        int hits = 0;
                        long lastUpdated = 0;
                        ArrayList<String> categories = new ArrayList<>();
                        while(reader.hasNext()) {
                            if(reader.peek() == JsonToken.NULL) {
                                reader.skipValue();
                                continue;
                            }
                            name = reader.nextName();
                            if(reader.peek() == JsonToken.NULL) {
                                reader.skipValue();
                                continue;
                            }
                            switch (name) {
                                case "i":
                                    id = reader.nextString();
                                    break;
                                case "t":
                                    title = reader.nextString();
                                    break;
                                case "im":
                                    image = reader.nextString();
                                    break;
                                case "h":
                                    hits = reader.nextInt();
                                    break;
                                case "ld":
                                    lastUpdated = (long) reader.nextDouble();
                                    break;
                                case "c":
                                    reader.beginArray();
                                    while (reader.hasNext()) {
                                        if(reader.peek() == JsonToken.NULL) {
                                            reader.skipValue();
                                            continue;
                                        }
                                        String str = reader.nextString().toLowerCase();
                                        categories.add(str);
                                        if (!allCategories.contains(str)) {
                                            allCategories.add(str);
                                        }
                                    }
                                    reader.endArray();
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();

                        title = title.equalsIgnoreCase("null") ? "-" : title;
                        image = image.equalsIgnoreCase("null") ? "" : image;

                        Comic comic = new Comic(id, title, image, lastUpdated, categories);
                        comic.hits = hits;
                        comics.comics.add(comic);
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();

            reader.close();

            Collections.sort(allCategories, String::compareTo);
            comics.categories = allCategories;

            Collections.sort(comics.comics, (c1, c2) -> Integer.compare(c2.hits, c1.hits));

            return comics;
        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Parses the chapters from given JSON string and loads them up into the given comic object.
     * @param comic The comic instance whose chapters are being loaded.
     * @param json Downloaded JSON string from server.
     * @return An instance of updated {@link Comic} if parsing was successful. NULL otherwise.
     */
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
                    comic.hits = jsonObject.getInt("hits");
                }

                if(jsonObject.has("released")) {
                    comic.released = jsonObject.getString("released");
                }

                Collections.sort(comic.chapters, (c1, c2)-> Integer.compare(c1.number, c2.number));

                return comic;
            }
        } catch (JSONException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parses the pages from given JSON string and loads them up into the given chapter object.
     * @param chapter The chapter instance whose pages are being loaded.
     * @param json Downloaded JSON string from server.
     * @return An instance of updated {@link Chapter} if parsing was successful. NULL otherwise.
     */
    @Nullable
    public static Chapter parseChapterPagesFromJSON(@NonNull Chapter chapter,
                                                    @NonNull String json) {
        try {
            if(json.length() > 0
                    && new JSONTokener(json).nextValue() instanceof JSONObject) {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has("images")
                        && new JSONTokener(jsonObject.getString("images")).nextValue()
                                    instanceof JSONArray) {
                    JSONArray imagesArray = jsonObject.getJSONArray("images");
                    int count = imagesArray.length();
                    chapter.pages.clear();
                    for(int i=0; i<count; i++) {
                        if(new JSONTokener(imagesArray.getString(i)).nextValue()
                                instanceof JSONArray) {
                            JSONArray arr = imagesArray.getJSONArray(i);
                            if(arr.length() >= 4) {
                                int number = arr.getInt(0);
                                String id = arr.getString(1);
                                int width = arr.getInt(2);
                                int height = arr.getInt(3);
                                chapter.pages.add(new Page(number, id, width, height));
                            }
                        }
                    }
                }

                Collections.sort(chapter.pages, (p1, p2) -> Integer.compare(p1.number, p2.number));

                return chapter;
            }
        } catch (JSONException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        return null;
    }

}
