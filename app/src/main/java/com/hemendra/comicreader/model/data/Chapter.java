package com.hemendra.comicreader.model.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Chapter implements Serializable {

    public String id;
    public int number;
    public String title;
    public long dateUpdated;

    public Chapter(String id, int number, String title, long dateUpdated) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.dateUpdated = dateUpdated;
    }

    public String getLastUpdatedString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(dateUpdated));
    }
}
