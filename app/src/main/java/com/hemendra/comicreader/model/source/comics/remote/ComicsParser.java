package com.hemendra.comicreader.model.source.comics.remote;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

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

                                if(new JSONTokener(mangaObject.getString("c")).nextValue()
                                        instanceof JSONArray) {
                                    JSONArray categoriesArray = mangaObject.getJSONArray("c");
                                    int len = categoriesArray.length();
                                    for(int x=0; x<len; x++) {
                                        categories.add(categoriesArray.getString(x));
                                    }
                                }

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

}
