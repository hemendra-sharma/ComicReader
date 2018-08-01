package com.hemendra.comicreader.model.data;

import android.support.annotation.Nullable;

import com.hemendra.comicreader.model.source.RemoteConfig;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Comic implements Serializable {

    public String id;
    public String title;
    private String image;
    private long lastUpdated;
    private ArrayList<String> categories;

    public String description = "";
    public String author = "";
    public String hits = "";
    public String released = "";
    public ArrayList<Chapter> chapters = new ArrayList<>();

    public Comic(String id, String title, String image,
                 long lastUpdated, ArrayList<String> categories) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.lastUpdated = lastUpdated;
        this.categories = categories;
    }

    public String getLastUpdatedString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(lastUpdated));
    }

    public String getCategoriesString(int max) {
        StringBuilder cat = new StringBuilder();
        for(int i=0; i<categories.size() && i<max; i++) {
            if(cat.length() > 0)
                cat.append(", ");
            cat.append(categories.get(i));
        }
        if(max < categories.size())
            cat.append("...");
        return cat.toString();
    }

    @Nullable
    public String getImageUrl() {
        if(image.length() > 0) {
            return RemoteConfig.buildImageUrl(image);
        }
        return null;
    }

}
