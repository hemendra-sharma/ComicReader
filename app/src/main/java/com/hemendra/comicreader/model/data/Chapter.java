package com.hemendra.comicreader.model.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Chapter implements Serializable {

    private static final long serialVersionUID = -11016580311731648L;

    public String id;
    public int number;
    public String title;
    public long dateUpdated;
    public ArrayList<Page> pages = new ArrayList<>();
    public int readingProgress = 0;

    public Chapter(String id, int number, String title, long dateUpdated) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.dateUpdated = dateUpdated * 1000L;
    }

    public String getLastUpdatedString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(dateUpdated));
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && obj instanceof Chapter
                && ((Chapter)obj).id.equals(id);
    }
}
