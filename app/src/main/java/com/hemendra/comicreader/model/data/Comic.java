package com.hemendra.comicreader.model.data;

import java.io.Serializable;
import java.util.ArrayList;

public class Comic implements Serializable {

    public String id;
    public String title;
    public String image;
    public long lastUpdated;
    public ArrayList<String> categories;
    public ArrayList<Chapter> chapters = new ArrayList<>();

    public Comic(String id, String title, String image,
                 long lastUpdated, ArrayList<String> categories) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.lastUpdated = lastUpdated;
        this.categories = categories;
    }

}
