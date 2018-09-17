package com.hemendra.comicreader.model.http;

public class HugeDownloadException extends Exception {

    public HugeDownloadException(String url, int totalLength) {
        super("Tried to download a content of length: "+totalLength+" bytes from URL: "+url);
        printStackTrace();
    }

}
