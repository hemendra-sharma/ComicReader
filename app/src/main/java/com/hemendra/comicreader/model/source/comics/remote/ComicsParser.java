package com.hemendra.comicreader.model.source.comics.remote;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.JsonToken;

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

public class ComicsParser {

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
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    comic.hits = jsonObject.getInt("hits");
                }

                if(jsonObject.has("released")) {
                    comic.released = jsonObject.getString("released");
                }

                Collections.sort(comic.chapters, (c1, c2)-> Integer.compare(c1.number, c2.number));

                return comic;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static Chapter parseChapterPagesFromJSON(@NonNull Chapter chapter, @NonNull String json) {
        try {
            if(new JSONTokener(json).nextValue() instanceof JSONObject) {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has("images")
                        && new JSONTokener(jsonObject.getString("images")).nextValue() instanceof JSONArray) {
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
                                chapter.pages.add(new Page(number, id));
                            }
                        }
                    }
                }

                Collections.sort(chapter.pages, (p1, p2) -> Integer.compare(p1.number, p2.number));

                return chapter;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
