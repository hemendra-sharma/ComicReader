package com.hemendra.comicreader.model.http;

import java.net.HttpURLConnection;

/**
 * This class can be used to get callbacks from {@link ContentDownloader}. Based on the callbacks
 * we can perform several actions, such as, cancelling the request, showing descriptive error
 * messages, updating UI, etc.
 */
public abstract class ConnectionCallback {

    /**
     * Gets called when the HttpUrlConnection failed to initialize (locally).
     */
    public void onFailedToInitialize(){}

    /**
     * Gets called when the connection has been initialized (locally) successfully.
     * @param conn The initialized connection object.
     */
    public void onConnectionInitialized(HttpURLConnection conn){}

    /**
     * Gets called when connection to server has been established and got a response HTTP status
     * code in return.
     * @param code HTTP Status code.
     */
    public void onResponseCode(int code){}

    /**
     * Gets called to acknowledge that it has started reading the response now.
     */
    public void onStartedReadingResponse(){}

    /**
     * Gets called to acknowledge that it has finished reading the response now.
     */
    public void onFinishedReadingResponse(){}
}
