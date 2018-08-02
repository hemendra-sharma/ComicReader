package com.hemendra.comicreader.model.data;

import android.support.annotation.Nullable;

import com.hemendra.comicreader.model.source.RemoteConfig;

import java.io.Serializable;

public class Page implements Serializable {

    public int number = 0;
    public String id = "";

    public Page(int number, String id) {
        this.number = number;
        this.id = id;
    }

    @Nullable
    public String getImageUrl() {
        if(id.length() > 0) {
            return RemoteConfig.buildImageUrl(id);
        }
        return null;
    }
}
