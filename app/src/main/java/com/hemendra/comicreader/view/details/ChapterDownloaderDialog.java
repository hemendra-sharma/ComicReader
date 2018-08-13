package com.hemendra.comicreader.view.details;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.images.remote.OnChapterDownloadListener;
import com.hemendra.comicreader.presenter.ComicsPresenter;

class ChapterDownloaderDialog {

    private Context context;
    private Chapter chapter;
    private ComicsPresenter presenter;

    private RelativeLayout mainRL;
    private TextView tvInfo, tvProgress1, tvProgress2;
    private ImageView ivProgress1, ivProgress2, ivDownload;
    private Dialog dialog;

    private Bitmap bmp1, bmp2;
    private int progressColor;

    ChapterDownloaderDialog(Context context, Chapter chapter, ComicsPresenter presenter,
                            ImageView ivDownload) {
        this.context = context;
        this.chapter = chapter;
        this.presenter = presenter;
        this.ivDownload = ivDownload;

        dialog = prepareLayout();

        progressColor = context.getResources().getColor(R.color.progressColor, null);
        bmp1 = Bitmap.createBitmap(100, 1, Bitmap.Config.ARGB_8888);
        bmp2 = Bitmap.createBitmap(100, 1, Bitmap.Config.ARGB_8888);
    }

    private Dialog prepareLayout() {
        View view = View.inflate(context, R.layout.dialog_chapter_downloader, null);

        mainRL = view.findViewById(R.id.mainRL);
        tvInfo = view.findViewById(R.id.tvInfo);
        tvProgress1 = view.findViewById(R.id.tvProgress1);
        tvProgress2 = view.findViewById(R.id.tvProgress2);
        ivProgress1 = view.findViewById(R.id.ivProgress1);
        ivProgress2 = view.findViewById(R.id.ivProgress2);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnCancel.setTransformationMethod(null);
        String str = context.getString(R.string.downloading_chapter_s, chapter.title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvInfo.setText(Html.fromHtml(str, 0));
        } else {
            tvInfo.setText(Html.fromHtml(str));
        }

        Dialog dialog = new Dialog(context, R.style.CategorySelectionDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(view);

        btnCancel.setOnClickListener(v->{
            presenter.stopDownloadingChapter();
            dialog.dismiss();
        });

        return dialog;
    }

    private Bitmap getProgressBitmap(Bitmap bmp, int progress) {
        for(int i=0; i<100; i++) {
            if(i <= progress)
                bmp.setPixel(i, 0, progressColor);
            else
                bmp.setPixel(i, 0, Color.LTGRAY);
        }
        return bmp;
    }

    private float bytesToMb(int numBytes) {
        float kb = (float) numBytes / 1024f;
        return kb / 1024f;
    }

    public void show() {
        if(dialog != null) {
            dialog.show();

            mainRL.getLayoutParams().width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);

            presenter.downloadChapter(chapter, new OnChapterDownloadListener() {
                @Override
                public void onChapterDownloaded(Chapter chapter) {
                    if(chapter.equals(ivDownload.getTag()))
                        ivDownload.setImageResource(R.drawable.ic_check);
                    dialog.dismiss();
                }

                @Override
                public void onProgressUpdate(Integer... progress) {
                    int chapterProgress = progress[0];
                    int downloadedPages = progress[1];
                    int totalPages = progress[2];
                    int pageProgress = progress[3];
                    int currentPageNumber = progress[4];
                    int numBytes = progress[5];

                    tvProgress1.setText(
                            context.getString(R.string.downloaded_x_out_of_y_pages_z,
                                    downloadedPages, totalPages, chapterProgress));
                    ivProgress1.setImageBitmap(getProgressBitmap(bmp1, chapterProgress));

                    tvProgress2.setText(
                            context.getString(R.string.downloading_page_no_x_y,
                                    currentPageNumber, pageProgress, bytesToMb(numBytes)));
                    ivProgress2.setImageBitmap(getProgressBitmap(bmp2, pageProgress));
                }

                @Override
                public void onFailedToDownloadChapter(FailureReason reason) {
                    if(chapter.equals(ivDownload.getTag()))
                        ivDownload.setImageResource(R.drawable.ic_download);
                    tvInfo.setText(R.string.failed_to_download_chapter_);
                }
            });
        }
    }

}
