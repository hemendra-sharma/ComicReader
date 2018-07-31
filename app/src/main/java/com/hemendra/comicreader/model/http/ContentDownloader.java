package com.hemendra.comicreader.model.http;

import android.support.annotation.WorkerThread;
import android.util.Log;

import com.hemendra.comicreader.model.http.tls.TlsHttp;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;

/**
 * This class is responsible to perform all kind of internet related operations.
 */
public class ContentDownloader {

    private static final String TAG = "ContentDownloader";

    /**
     * Fetches the web content of given URL as String
     * @param url web url
     * @return null if interrupted, any network errors occurred, or not 200 OK.
     * Else returns the web content as String.
     */
    @WorkerThread
    public static String downloadAsString(String url) {
        return downloadAsString(url, null);
    }

    /**
     * Fetches the web content of given URL as String
     * @param url web url
     * @param callback HTTP Connection callback methods
     * @return null if interrupted, any network errors occurred, or not 200 OK.
     * Else returns the web content as String.
     */
    @WorkerThread
    public static String downloadAsString(String url, ConnectionCallback callback) {
        HttpURLConnection conn = null;
        ByteArrayOutputStream output = null;
        BufferedInputStream input = null;
        String response = null;
        try {
            if(url != null && url.trim().length() > 0)
                url = url.replace(" ", "%20");

            Log.d("downloading string", ">> " + url);

            conn = TlsHttp.getConnection("GET", url);
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
                    Log.d(TAG,
                            "Failed to Download URL as String. Response code: " + conn.getResponseCode());
                }
            } else {
                if(callback != null)
                    callback.onFailedToInitialize();
                Log.d(TAG,
                        "Failed to Download URL as String. 'conn' is null.");
            }
        } catch (InterruptedIOException ignore) {
            // ignore
            Log.d(TAG,"Interrupted while downloading URL '"+url+"'");
        } catch(Throwable ex){
            Log.d(TAG,"Failed to download content as String. Requested URL '"+url+"'");
            ex.printStackTrace();
        } finally {
            try {
                if(conn != null)
                    conn.disconnect();
                if(input != null)
                    input.close();
                if(output != null)
                    output.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    /**
     * Fetches the web content of given URL as String. Also notify the download progress with callback.
     * @param url web url
     * @param task An instance of {@link CustomAsyncTask} to publish the download progress.
     *             This instance must have the progress parameter of type {@link Float}.
     *             To indicate the progress, this method returns progress percentages as:
     *                 [-1 = Connection Established. Waiting for response.]
     *                 , and [Above 0 = Download in progress]
     * @return null if interrupted, any network errors occurred, or not 200 OK.
     * Else returns the web content as String.
     */
    @WorkerThread
    public static String downloadAsLargeString(String url, CustomAsyncTask<?,Float,?> task) {
        HttpURLConnection conn = null;
        ByteArrayOutputStream output = null;
        BufferedInputStream input = null;
        String response = null;
        try {
            if(url != null && url.trim().length() > 0)
                url = url.replace(" ", "%20");

            Log.d("downloading string", ">> " + url);

            conn = TlsHttp.getConnection("GET", url);
            if(conn != null) {
                conn.connect();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    long downloaded = 0;
                    long totalLength = conn.getContentLength();
                    if (totalLength <= 0)
                        throw (new Exception("The URL is not returning content-length"));
                    //
                    if (task != null)
                        task.publishProgress(-1f, (float) downloaded, (float) totalLength);
                    //
                    input = new BufferedInputStream(conn.getInputStream());
                    output = new ByteArrayOutputStream();
                    byte data[] = new byte[1024];
                    int read;
                    //
                    if (task != null)
                        task.publishProgress(0f, (float) downloaded, (float) totalLength);
                    //
                    long lastProgressUpdated = 0;
                    long now;
                    while ((read = input.read(data)) != -1 && !Thread.interrupted()) {
                        output.write(data, 0, read);
                        downloaded += read;
                        if (task != null
                                && (now = System.currentTimeMillis()) - lastProgressUpdated > 500) {
                            float percent = (float) ((double) downloaded / (double) totalLength) * 100f;
                            Log.d("percent", ">> " + percent + " = " + downloaded + " / " + totalLength);
                            percent = percent > 100f ? 100f : percent < 0 ? 0 : percent;
                            task.publishProgress(percent, (float) downloaded, (float) totalLength);
                            lastProgressUpdated = now;
                        }
                    }
                    response = new String(output.toByteArray());
                    input.close();
                    input = null;
                    output.close();
                    output = null;
                } else {
                    Log.d(TAG,
                            "Failed to Download URL as String. Response code: " + conn.getResponseCode());
                }
            } else {
                Log.d(TAG,
                        "Failed to Download URL as String. 'conn' is null.");
            }
        } catch (InterruptedIOException ignore) {
            // ignore
            Log.d(TAG, "Interrupted while downloading URL '"+url+"'");
        } catch(Throwable ex){
            Log.d(TAG, "Failed to download content as String. Requested URL '"+url+"'");
            ex.printStackTrace();
        } finally {
            try {
                if(conn != null)
                    conn.disconnect();
                if(input != null)
                    input.close();
                if(output != null)
                    output.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return response;
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
            if(url != null && url.trim().length() > 0)
                url = url.replace(" ", "%20");

            Log.d("downloading bytes", ">> "+url);

            conn = TlsHttp.getConnection("GET", url);
            if(conn != null) {
                if(callback != null)
                    callback.onConnectionInitialized(conn);
                conn.connect();
                if(callback != null)
                    callback.onResponseCode(conn.getResponseCode());
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedInputStream(conn.getInputStream());
                    outStream = new ByteArrayOutputStream();
                    int read;
                    if(callback != null)
                        callback.onStartedReadingResponse();
                    byte[] buff = new byte[1024];
                    while ((read = reader.read(buff)) > 0
                            && !Thread.interrupted()) {
                        outStream.write(buff, 0, read);
                    }
                    bytes = outStream.toByteArray();
                    if(callback != null)
                        callback.onFinishedReadingResponse();
                    reader.close();
                    reader = null;
                    outStream.close();
                    outStream = null;
                } else {
                    Log.d(TAG, "Failed to Download URL as Raw Bytes. Response code: " + conn.getResponseCode());
                }
            } else {
                if(callback != null)
                    callback.onFailedToInitialize();
                Log.d(TAG, "Failed to Download URL as Raw Bytes. 'conn' is null.");
            }
        } catch (InterruptedIOException ignore) {
            // ignore
            Log.d(TAG, "Interrupted while downloading URL '"+url+"'");
        } catch(Throwable ex){
            Log.d(TAG, "Failed to download content as byte array. Requested URL '"+url+"'");
            ex.printStackTrace();
        } finally {
            try {
                if(conn != null)
                    conn.disconnect();
                if(reader != null)
                    reader.close();
                if(outStream != null)
                    outStream.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * Fetches the web content of given URL and saves it as the given file.
     * @param url web url
     * @return TRUE if successful, else FALSE otherwise.
     */
    @WorkerThread
    public static boolean downloadAsFile(String url, File outFile) {
        HttpURLConnection conn = null;
        FileOutputStream outStream = null;
        BufferedInputStream reader = null;
        try {
            if(url != null && url.trim().length() > 0)
                url = url.replace(" ", "%20");

            Log.d("downloading file", ">> "+url);

            conn = TlsHttp.getConnection("GET", url);
            if(conn != null) {
                conn.connect();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedInputStream(conn.getInputStream());
                    outStream = new FileOutputStream(outFile);
                    int read;
                    byte[] buff = new byte[1024];
                    while ((read = reader.read(buff)) > 0
                            && !Thread.interrupted()) {
                        outStream.write(buff, 0, read);
                    }
                    reader.close();
                    reader = null;
                    outStream.close();
                    outStream = null;
                    if (outFile.exists()) {
                        return true;
                    }
                } else {
                    Log.d(TAG,
                            "Failed to Download URL as File. Response code: " + conn.getResponseCode());
                }
            } else {
                Log.d(TAG,
                        "Failed to Download URL as File. 'conn' is null.");
            }
        } catch (InterruptedIOException ignore) {
            // ignore
            Log.d(TAG, "Interrupted while downloading URL '"+url+"'");
        } catch(Throwable ex){
            Log.d(TAG, "Failed to download content as File. Requested URL '"+url+"'");
            ex.printStackTrace();
        } finally {
            try {
                if(conn != null)
                    conn.disconnect();
                if(reader != null)
                    reader.close();
                if(outStream != null)
                    outStream.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
