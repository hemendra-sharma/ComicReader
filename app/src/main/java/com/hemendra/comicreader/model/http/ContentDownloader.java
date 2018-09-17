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

package com.hemendra.comicreader.model.http;

import android.support.annotation.WorkerThread;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;

/**
 * This class is responsible to perform all kind of internet related operations.
 * @author Hemendra Sharma
 */
public class ContentDownloader {

    private static final String TAG = "ContentDownloader";

    /**
     * Fetches the web content of given URL as String
     * @param url web url
     * @param callback HTTP Connection callback methods
     * @return returns null if interrupted, any network errors occurred, or not 200 OK.
     * Else returns the web content as String.
     */
    @WorkerThread
    public static String downloadAsString(String url, ConnectionCallback callback) {
        HttpURLConnection conn = null;
        ByteArrayOutputStream output = null;
        BufferedInputStream input = null;
        String response = null;
        try {
            if(url == null || url.trim().length() == 0) {
                Log.e(TAG, "'null' URL cannot be downloaded");
                return null;
            }

            url = url.replace(" ", "%20");

            Log.d("downloading string", ">> " + url);

            conn = ConnectionBuilder.getConnection("GET", url);
            if(conn != null) {
                if(callback != null)
                    callback.onConnectionInitialized(conn);
                conn.connect();
                if(callback != null)
                    callback.onResponseCode(conn.getResponseCode());
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    input = new BufferedInputStream(conn.getInputStream());
                    output = new ByteArrayOutputStream();
                    byte data[] = new byte[1024];
                    int read;
                    if(callback != null)
                        callback.onStartedReadingResponse();
                    while ((read = input.read(data)) != -1 && !Thread.interrupted()) {
                        output.write(data, 0, read);
                    }
                    response = new String(output.toByteArray());
                    if(callback != null)
                        callback.onFinishedReadingResponse();
                    input.close();
                    input = null;
                    output.close();
                    output = null;
                } else {
                    Log.e(TAG, "Failed to Download URL as String. Response code: " + conn.getResponseCode());
                }
            } else {
                if(callback != null)
                    callback.onFailedToInitialize();
                Log.e(TAG, "Failed to Download URL as String. 'conn' is null.");
            }
        } catch (InterruptedIOException e) {
            // don't do anything. It was interrupted intentionally.
        } catch(IOException e){
            Log.e(TAG, "Failed to download content as String. Requested URL '"+url+"'");
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            try {
                if(conn != null)
                    conn.disconnect();
                if(input != null)
                    input.close();
                if(output != null)
                    output.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }
        return response;
    }

    /**
     * Fetches the web content of given URL as InputStream
     * @param url web url
     * @param callback HTTP Connection callback methods
     * @return null if interrupted, any network errors occurred, or not 200 OK.
     * Else returns the web content as String.
     */
    @WorkerThread
    public static InputStream downloadAsStream(String url, ConnectionCallback callback) {
        HttpURLConnection conn;
        try {
            if(url == null || url.trim().length() == 0) {
                Log.e(TAG, "'null' URL cannot be downloaded");
                return null;
            }

            url = url.replace(" ", "%20");

            Log.d("downloading string", ">> " + url);

            conn = ConnectionBuilder.getConnection("GET", url);
            if(conn != null) {
                if(callback != null)
                    callback.onConnectionInitialized(conn);
                conn.connect();
                if(callback != null)
                    callback.onResponseCode(conn.getResponseCode());
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return conn.getInputStream();
                } else {
                    Log.e(TAG,
                            "Failed to Download URL as String. Response code: " + conn.getResponseCode());
                }
            } else {
                if(callback != null)
                    callback.onFailedToInitialize();
                Log.e(TAG,
                        "Failed to Download URL as String. 'conn' is null.");
            }
        } catch (InterruptedIOException e) {
            // don't do anything. It was interrupted intentionally.
        } catch(IOException e){
            Log.e(TAG,"Failed to download content as String. Requested URL '"+url+"'");
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetches the web content of given URL as raw byte array.
     * @param url web url
     * @param callback HTTP Connection callback methods
     * @return null if interrupted, any network errors occurred, or not 200 OK.
     * Else returns the web content as byte array.
     */
    @WorkerThread
    public static byte[] downloadAsByteArray(String url, ConnectionCallback callback) {
        HttpURLConnection conn = null;
        BufferedInputStream reader = null;
        ByteArrayOutputStream outStream = null;
        byte[] bytes = new byte[0];
        try {
            if(url == null || url.trim().length() == 0) {
                Log.e(TAG, "'null' URL cannot be downloaded");
                return null;
            }

            url = url.replace(" ", "%20");

            Log.d("downloading bytes", ">> "+url);

            conn = ConnectionBuilder.getConnection("GET", url);
            if(conn != null) {
                if(callback != null)
                    callback.onConnectionInitialized(conn);
                conn.connect();
                if(callback != null)
                    callback.onResponseCode(conn.getResponseCode());
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    int total = conn.getContentLength();
                    int _5_mb = 5 * 1024 * 1024;
                    if(total > _5_mb) {
                        Crashlytics.logException(new HugeDownloadException(url, total));
                        return bytes;
                    }
                    int totalRead = 0;
                    reader = new BufferedInputStream(conn.getInputStream());
                    outStream = new ByteArrayOutputStream();
                    int read;
                    if(callback != null)
                        callback.onStartedReadingResponse();
                    byte[] buff = new byte[1024];
                    while ((read = reader.read(buff)) > 0
                            && !Thread.interrupted()) {
                        outStream.write(buff, 0, read);
                        if(callback != null && total > 0) {
                            totalRead += read;
                            float percent = ((float) totalRead / (float) total) * 100f;
                            callback.onProgress(percent, total);
                        }
                    }
                    bytes = outStream.toByteArray();
                    if(callback != null)
                        callback.onFinishedReadingResponse();
                    //
                    if(bytes.length > _5_mb) {
                        Crashlytics.logException(new HugeDownloadException(url, total));
                        return new byte[0];
                    }
                } else {
                    Log.e(TAG, "Failed to Download URL as Raw Bytes. Response code: " + conn.getResponseCode());
                }
            } else {
                if(callback != null)
                    callback.onFailedToInitialize();
                Log.e(TAG, "Failed to Download URL as Raw Bytes. 'conn' is null.");
            }
        } catch (InterruptedIOException e) {
            // don't do anything. It was interrupted intentionally.
        } catch (IOException e) {
            Log.e(TAG,"Failed to download content as String. Requested URL '"+url+"'");
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            try {
                if(conn != null)
                    conn.disconnect();
                if(reader != null)
                    reader.close();
                if(outStream != null)
                    outStream.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }
        return bytes;
    }

}
