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

import java.net.HttpURLConnection;

/**
 * This class can be used to get callbacks from {@link ContentDownloader}. Based on the callbacks
 * we can perform several actions, such as, cancelling the request, showing descriptive error
 * messages, updating UI, etc.
 * @author Hemendra Sharma
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
     * Gets called when the download progress has changed
     * @param progress Float progress value in percentage
     */
    public void onProgress(float progress, int totalLength){}

    /**
     * Gets called to acknowledge that it has started reading the response now.
     */
    public void onStartedReadingResponse(){}

    /**
     * Gets called to acknowledge that it has finished reading the response now.
     */
    public void onFinishedReadingResponse(){}
}
