package com.hemendra.comicreader.model.http.tls;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * Some of the HTTPS connections use TLS. So, this class is going to handle the TLS
 * enabled HTTPS connections.
 */
public class TlsHttp {

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
            } catch (Throwable ex) {
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
        }catch (Throwable ex){
            ex.printStackTrace();
        }
        return conn;
    }

}
