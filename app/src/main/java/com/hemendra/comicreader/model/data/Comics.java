package com.hemendra.comicreader.model.data;

import java.io.Serializable;
import java.util.ArrayList;

public class Comics implements Serializable {

    public ArrayList<String> categories = new ArrayList<>();
    public ArrayList<Comic> comics = new ArrayList<>();

    public int count() {
        return comics.size();
    }

}
