package com.hemendra.comicreader.model.source;

import android.content.Context;

public abstract class DataSource {

    private Context context;

    public DataSource(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return this.context;
    }

    public abstract void dispose();
}
