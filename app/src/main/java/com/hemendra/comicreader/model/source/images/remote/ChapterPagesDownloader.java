package com.hemendra.comicreader.model.source.images.remote;

import android.content.Context;
import android.os.Environment;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Page;
import com.hemendra.comicreader.model.http.ConnectionCallback;
import com.hemendra.comicreader.model.http.ContentDownloader;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.RemoteConfig;
import com.hemendra.comicreader.model.source.comics.remote.ComicsParser;
import com.hemendra.comicreader.model.source.images.local.ImagesDB;
import com.hemendra.comicreader.model.utils.CustomAsyncTask;
import com.hemendra.comicreader.model.utils.Utils;

import java.io.File;
import java.net.HttpURLConnection;

public class ChapterPagesDownloader extends CustomAsyncTask<Void,Float,Boolean> {

    private Context context;
    private OnChapterDownloadListener listener;
    private Chapter chapter;
    private FailureReason reason = FailureReason.UNKNOWN_REMOTE_ERROR;
    private float progress1 = 0;
    private ImagesDB db;

    public ChapterPagesDownloader(Context context,
                                  OnChapterDownloadListener listener,
                                  Chapter chapter) {
        this.context = context;
        this.listener = listener;
        this.chapter = chapter;
        this.db = new ImagesDB(context).open();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if(chapter.pages.size() == 0) {
                String json = ContentDownloader.downloadAsString(RemoteConfig.buildChapterUrl(chapter.id),
                        new ConnectionCallback() {
                            @Override
                            public void onResponseCode(int code) {
                                switch (code) {
                                    case HttpURLConnection.HTTP_NOT_FOUND:
                                        reason = FailureReason.API_MISSING;
                                        break;
                                    default:
                                        reason = FailureReason.INVALID_RESPONSE_FROM_SERVER;
                                }
                            }
                        });
                if(json != null) {
                    chapter = ComicsParser.parseChapterPagesFromJSON(chapter, json);
                }
            }
            //
            if(chapter != null && chapter.pages.size() > 0) {
                boolean allSuccess = true;
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/" + context.getPackageName() + "/cache/chapters");
                File file = new File(dir, chapter.id + ".obj");
                if (!file.exists() || file.length() == 0) {
                    if (dir.exists() || dir.mkdirs()) {
                        int count = 0;
                        for (Page page : chapter.pages) {
                            progress1 = ((float) count / (float) chapter.pages.size()) * 100f;
                            publishProgress(progress1, 0f);

                            String imgUrl = page.getImageUrl();

                            page.rawImageData = db.getPage(imgUrl);

                            if (page.rawImageData == null || page.rawImageData.length == 0) {
                                byte[] bytes = ContentDownloader.downloadAsByteArray(imgUrl,
                                        new ConnectionCallback() {
                                            @Override
                                            public void onProgress(float progress) {
                                                publishProgress(progress1, progress);
                                            }

                                            @Override
                                            public void onResponseCode(int code) {
                                                switch (code) {
                                                    case HttpURLConnection.HTTP_NOT_FOUND:
                                                        reason = FailureReason.CONTENT_MISSING;
                                                        break;
                                                    default:
                                                        reason = FailureReason.INVALID_RESPONSE_FROM_SERVER;
                                                }
                                            }
                                        });
                                if (bytes != null && bytes.length > 0) {
                                    page.rawImageData = bytes;
                                    db.insertPage(imgUrl, bytes);
                                } else {
                                    allSuccess = false;
                                    break;
                                }
                            }

                            count++;
                        }
                        if(allSuccess) {
                            allSuccess = Utils.writeToFile(chapter, file);
                        }
                    }
                }
                return allSuccess;
            }
        }catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Float... progress) {
        listener.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        db.close();
        if(success) {
            listener.onChapterDownloaded(chapter);
        } else {
            listener.onFailedToDownloadChapter(reason);
        }
    }
}
