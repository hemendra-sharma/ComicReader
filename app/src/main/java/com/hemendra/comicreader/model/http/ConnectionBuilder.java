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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * Builds the instance of {@link HttpURLConnection} or {@link HttpsURLConnection} depending
 * on the input url (http:// or https://).
 */
public class ConnectionBuilder {

    /**
     * Get the {@link HttpsURLConnection} object.
     * @param httpMethod "GET" or "POST"
     * @param url The URL we want to connect.
     * @return the {@link HttpURLConnection} or {@link HttpsURLConnection} object depending on the
     * given URL.
     */
    @Nullable
    public static HttpURLConnection getConnection(@NonNull String httpMethod, @NonNull String url) {
        if(url.startsWith("http:"))
            return getUnsecuredConnection(httpMethod, url);
        else if(url.startsWith("https:")) {
            HttpsURLConnection conn = null;
            try {
                conn = (HttpsURLConnection) new URL(url).openConnection();

                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, null, new java.security.SecureRandom());
                javax.net.ssl.SSLSocketFactory socketFactory = sc.getSocketFactory();
                conn.setSSLSocketFactory(socketFactory);
                //
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setRequestMethod(httpMethod);
                if (httpMethod.equalsIgnoreCase("POST"))
                    conn.setDoOutput(true);
                conn.setDoInput(true);
            } catch (NoSuchAlgorithmException
                    | IOException
                    | KeyManagementException ex) {
                ex.printStackTrace();
            }
            return conn;
        } else {
            return null;
        }
    }

    /**
     * Created and return the object instance of {@link HttpURLConnection}
     * @param httpMethod "GET" or "POST"
     * @param url The URL we want to connect.
     * @return the {@link HttpURLConnection} object.
     */
    private static HttpURLConnection getUnsecuredConnection(String httpMethod, String url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestMethod(httpMethod);
            if(httpMethod.equalsIgnoreCase("POST"))
                conn.setDoOutput(true);
            conn.setDoInput(true);
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return conn;
    }

}
