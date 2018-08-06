package com.hemendra.comicreader.model.data;

import java.io.Serializable;
import java.util.ArrayList;

public class Comics implements Serializable {

    private static final long serialVersionUID = 4603566027627855242L;

    public ArrayList<String> categories = new ArrayList<>();
    public ArrayList<Comic> comics = new ArrayList<>();

}
