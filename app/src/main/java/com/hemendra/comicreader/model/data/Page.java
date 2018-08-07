package com.hemendra.comicreader.model.data;

import android.support.annotation.Nullable;

import com.hemendra.comicreader.model.source.RemoteConfig;

import java.io.Serializable;

public class Page implements Serializable {

    private static final long serialVersionUID = -5997692223648746465L;

    public int number;
    public String id;
    public byte[] rawImageData = null;

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
