/*
 * Copyright (c) 2018 Hemendra Sharma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hemendra.comicreader.model.source.images.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * We are going to convert them in blob (byte array) and save them into the database itself.
 * This way we can query and manage the images more easily and efficiently.
 */
public class ImagesDB {

    /**
     * Maximum number of cover images we want to hold up in the cache.
     */
    private static final int MAX_CACHED_IMAGES = 500;
    /**
     * Maximum number of comic book pages we want to hold up in the cache.
     */
    private static final int MAX_CACHED_PAGES = 100;

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "ImagesDB";

    private static final String TAB_IMAGES = "tab_images";
    private static final String TAB_PAGES = "tab_pages";

    private static final String CREATE_TAB_IMAGES = "create table if not exists  "
            + TAB_IMAGES
            + " ("
            + "_id integer primary key autoincrement,"  // primary key ID
            + "url text, "                              // the URL can be assumed unique
            + "data blob );";                           // byte array image data

    private static final String CREATE_TAB_PAGES = "create table if not exists  "
            + TAB_PAGES
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
            db.execSQL("PRAGMA foreign_keys=ON");
            db.execSQL(CREATE_TAB_IMAGES);
            db.execSQL(CREATE_TAB_PAGES);
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

        private void UpgradeDowngrade(SQLiteDatabase db,
                                      int oldVersion,
                                      int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TAB_IMAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TAB_PAGES);
            onCreate(db);
        }
    }


    /**
     * Open a new connection to database.
     * @return a new instance of {@link ImagesDB}
     */
    public ImagesDB open() {
        if (DBHelper != null) {
            db = DBHelper.getWritableDatabase();
            return this;
        }
        return null;
    }

    /**
     * Close the existing open connection.
     */
    public void close() {
        db.close();
    }

    /**
     * Insert the new image data byte array, if it does not exist already.
     * @param url the URL from where this image was downloaded.
     * @param data image byte array
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insertImage(String url, byte[] data) {
        if (url != null && url.trim().length() > 0) {
            int count = 0;
            String countQuery = "SELECT count(*) FROM " + TAB_IMAGES + " WHERE url='" + url.trim() + "'";
            Cursor c = db.rawQuery(countQuery, null);
            if (c != null) {
                if (c.moveToFirst() && c.getColumnCount() > 0
                        && !c.isNull(0)) {
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
        return -1;
    }

    /**
     * Keep the latest MAX_CACHED_IMAGES images and delete all other old images.
     */
    private void keepLastMaxImagesOnly() {
        String countQuery = "SELECT count(*) FROM "+TAB_IMAGES;
        Cursor c = db.rawQuery(countQuery, null);
        if(c != null) {
            if(c.moveToFirst() && c.getColumnCount() > 0
                    && !c.isNull(0)) {
                int totalImages = c.getInt(0);
                c.close();
                if(totalImages > MAX_CACHED_IMAGES) {
                    String query = "SELECT _id FROM " + TAB_IMAGES + " ORDER BY _id ASC";
                    c = db.rawQuery(query, null);
                    if (c != null) {
                        if (c.moveToFirst() && c.getColumnCount() > 0
                                && !c.isNull(0)) {
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
    }

    /**
     * Check if the image with the given URL exists in the DB or not,
     * and return the image byte array.
     * @param url the URL which is supposed to be used to download image from server.
     * @return the image byte array if exists. NULL otherwise.
     */
    public byte[] getImage(String url) {
        byte[] bytes = null;
        if(url != null && url.trim().length() > 0) {
            Cursor c = db.rawQuery("select * from " + TAB_IMAGES + " WHERE url='" + url.trim() + "'", null);
            if (c != null) {
                if (c.moveToFirst() && c.getColumnCount() >= 3
                        && !c.isNull(2)) {
                    bytes = c.getBlob(2);
                }
                c.close();
            }
        }
        return bytes;
    }

    /**
     * Insert the new image data byte array, if it does not exist already.
     * @param url the URL from where this image was downloaded.
     * @param data image byte array
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insertPage(String url, byte[] data) {
        if (url != null && url.trim().length() > 0) {
            int count = 0;
            String countQuery = "SELECT count(*) FROM " + TAB_PAGES + " WHERE url='" + url.trim() + "'";
            Cursor c = db.rawQuery(countQuery, null);
            if (c != null) {
                if (c.moveToFirst() && c.getColumnCount() > 0
                        && !c.isNull(0)) {
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
                    long ret = db.insert(TAB_PAGES, null, values);
                    if (ret > 0) {
                        keepLastMaxPagesOnly();
                        return ret;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Keep the latest MAX_CACHED_IMAGES images and delete all other old images.
     */
    private void keepLastMaxPagesOnly() {
        String countQuery = "SELECT count(*) FROM "+TAB_PAGES;
        Cursor c = db.rawQuery(countQuery, null);
        if(c != null) {
            if(c.moveToFirst() && c.getColumnCount() > 0
                    && !c.isNull(0)) {
                int totalImages = c.getInt(0);
                c.close();
                if(totalImages > MAX_CACHED_PAGES) {
                    String query = "SELECT _id FROM " + TAB_PAGES + " ORDER BY _id ASC";
                    c = db.rawQuery(query, null);
                    if (c != null) {
                        if (c.moveToFirst() && c.getColumnCount() > 0
                                && !c.isNull(0)) {
                            int diff = totalImages - MAX_CACHED_PAGES;
                            int count = 0;
                            do {
                                db.delete(TAB_PAGES, "_id="+c.getInt(0), null);
                                count++;
                            } while (c.moveToNext() && count < diff);
                        }
                        c.close();
                    }
                }
            }
        }
    }

    /**
     * Check if the image with the given URL exists in the DB or not,
     * and return the image byte array.
     * @param url the URL which is supposed to be used to download image from server.
     * @return the image byte array if exists. NULL otherwise.
     */
    public byte[] getPage(String url) {
        byte[] bytes = null;
        if(url != null && url.trim().length() > 0) {
            Cursor c = db.rawQuery("select * from " + TAB_PAGES + " WHERE url='" + url.trim() + "'", null);
            if (c != null) {
                if (c.moveToFirst() && c.getColumnCount() >= 3
                        && !c.isNull(2)) {
                    bytes = c.getBlob(2);
                }
                c.close();
            }
        }
        return bytes;
    }

    public boolean hasPage(String url) {
        int count = 0;
        if(url != null && url.trim().length() > 0) {
            Cursor c = db.rawQuery("select count(*) from " + TAB_PAGES + " WHERE url='" + url.trim() + "'", null);
            if (c != null) {
                if (c.moveToFirst() && c.getColumnCount() > 0
                        && !c.isNull(0)) {
                    count = c.getInt(0);
                }
                c.close();
            }
        }
        return count > 0;
    }

}
