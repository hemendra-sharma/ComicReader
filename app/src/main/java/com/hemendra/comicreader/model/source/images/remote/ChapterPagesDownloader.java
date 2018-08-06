package com.hemendra.comicreader.model.source.images.remote;

import android.content.Context;
import android.widget.ImageView;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Page;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.source.images.local.ImagesDB;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;

public class ChapterPagesDownloader extends CustomAsyncTask<Void,Void,Boolean> {

    private OnPagesDownloadedListener listener;
    private Chapter chapter;
    private ImageView iv;
    private ImagesDB db;

    public ChapterPagesDownloader(Context context,
                                  OnPagesDownloadedListener listener,
                                  Chapter chapter, ImageView iv) {
        this.listener = listener;
        this.chapter = chapter;
        this.iv = iv;
        this.db = new ImagesDB(context).open();
    }

    public boolean isForChapter(Chapter chapter) {
        return this.chapter.equals(chapter);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            boolean allSuccess = true;
            for(Page page : chapter.pages) {
                String imgUrl = page.getImageUrl();
                if(db.hasPage(imgUrl))
                    continue;

                byte[] bytes = ContentDownloader.downloadAsByteArray(imgUrl, null);
                if (bytes != null && bytes.length > 0) {
                    db.insertPage(imgUrl, bytes);
                } else {
                    allSuccess = false;
                    break;
                }
            }
            return allSuccess;
        }catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if(success) {
            listener.onPagesDownloaded(chapter, iv);
        } else {
            listener.onFailedToDownloadPages(chapter, iv);
        }
    }
}
