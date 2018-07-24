package com.hemendra.comicreader.model.source.images.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * After looking at several images from openlibrary.org, it looks like the images are
 * always going to have small data sizes. So, instead of saving the images on disk, we are
 * going to convert them in blob (byte array) and save them into the database itself.
 * This way we can query and manage the images more easily and efficiently.
 *
 * Another benefit of doing this? We won't need to implement the Runtime-Permission thing.
 */
public class ImagesDB {

    private static final int MAX_CACHED_IMAGES = 100;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ImagesDB";

    private static final String TAB_IMAGES = "tab_images";

    private static final String CREATE_TAB_IMAGES = "create table if not exists  "
            + TAB_IMAGES
            + " ("
            + "_id integer primary key autoincrement,"  // primary key ID
            + "url text, "                              // the URL can be assumed unique
            + "data blob );";                           // byte array image data

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public ImagesDB(Context ctx) {
        DBHelper = new DatabaseHelper(ctx);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("PRAGMA foreign_keys=ON");
                db.execSQL(CREATE_TAB_IMAGES);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            UpgradeDowngrade(db, oldVersion, newVersion);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion,
                                int newVersion) {
            UpgradeDowngrade(db, oldVersion, newVersion);
        }

        private void UpgradeDowngrade(SQLiteDatabase db, int oldVersion,
                                      int newVersion) {
            try {
                //
                // when upgrading or downgrading we can migrate the data into new format
                // conditionally. But for now we are just going to drop the whole cache,
                // because this app is going to have only this version.
                //
                db.execSQL("DROP TABLE IF EXISTS " + TAB_IMAGES);
                onCreate(db);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * Open a new connection to database.
     * @return a new instance of {@link ImagesDB}
     */
    public ImagesDB open() {
        try {
            if (DBHelper != null) {
                db = DBHelper.getWritableDatabase();
                return this;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Close the existing open connection.
     */
    public void close() {
        if (db != null)
            db.close();
    }

    /**
     * Insert the new image data byte array, if it does not exist already.
     * @param url the URL from where this image was downloaded.
     * @param data image byte array
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insertImage(String url, byte[] data) {
        try {
            if (url != null && url.trim().length() > 0) {
                int count = 0;
                String countQuery = "SELECT count(*) FROM " + TAB_IMAGES + " WHERE url='" + url.trim() + "'";
                Cursor c = db.rawQuery(countQuery, null);
                if (c != null) {
                    if (c.moveToFirst()) {
                        count = c.getInt(0);
                    }
                    c.close();
                }
                //
                if(count <= 0) {
                    ContentValues values = new ContentValues();
                    if (data != null && data.length > 0) {
                        values.put("url", url);
                        values.put("data", data);
                        long ret = db.insert(TAB_IMAGES, null, values);
                        if (ret > 0) {
                            keepLastMaxImagesOnly();
                            return ret;
                        }
                    }
                }
            }
        } catch(Throwable ex){
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * Keep the latest MAX_CACHED_IMAGES images and delete all other old images.
     */
    private void keepLastMaxImagesOnly() {
        try {
            String countQyery = "SELECT count(*) FROM "+TAB_IMAGES;
            Cursor c = db.rawQuery(countQyery, null);
            if(c != null) {
                if(c.moveToFirst()) {
                    int totalImages = c.getInt(0);
                    c.close();
                    if(totalImages > MAX_CACHED_IMAGES) {
                        String query = "SELECT _id FROM " + TAB_IMAGES + " ORDER BY _id ASC";
                        c = db.rawQuery(query, null);
                        if (c != null) {
                            if (c.moveToFirst()) {
                                int diff = totalImages - MAX_CACHED_IMAGES;
                                int count = 0;
                                do {
                                    db.delete(TAB_IMAGES, "_id="+c.getInt(0), null);
                                    count++;
                                } while (c.moveToNext() && count < diff);
                            }
                            c.close();
                        }
                    }
                }
            }
        } catch (Throwable ex){
            ex.printStackTrace();
        }
    }

    /**
     * Check if the image with the given URL exists in the DB or not,
     * and return the image byte array.
     * @param url the URL which is supposed to be used to download image from server.
     * @return the image byte array if exists. NULL otherwise.
     */
    public byte[] getImage(String url) {
        byte[] bytes = null;
        try {
            if(url != null && url.trim().length() > 0) {
                Cursor c = db.rawQuery("select * from " + TAB_IMAGES + " WHERE url='" + url.trim() + "'", null);
                if (c != null) {
                    if (c.moveToFirst()) {
                        bytes = c.getBlob(2);
                    }
                    c.close();
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

}
