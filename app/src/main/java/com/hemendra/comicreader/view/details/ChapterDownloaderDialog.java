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

import java.util.Locale;

public class ChapterDownloaderDialog {

    private Context context;
    private Chapter chapter;
    private ComicsPresenter presenter;

    private RelativeLayout mainRL;
    private TextView tvInfo, tvProgress1, tvProgress2;
    private ImageView ivProgress1, ivProgress2, ivDownload;
    private Dialog dialog;

    private int progressColor = 0;

    public ChapterDownloaderDialog(Context context, Chapter chapter, ComicsPresenter presenter,
                                   ImageView ivDownload) {
        this.context = context;
        this.chapter = chapter;
        this.presenter = presenter;
        this.ivDownload = ivDownload;

        dialog = prepareLayout();

        progressColor = context.getResources().getColor(R.color.progressColor, null);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvInfo.setText(Html.fromHtml("Downloading Chapter<br><br><b>\""+chapter.title+"\"</b>", 0));
        } else {
            tvInfo.setText(Html.fromHtml("Downloading Chapter<br><br><b>\""+chapter.title+"\"</b>"));
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

    private Bitmap getProgressBitmap(float progress) {
        int p = (int) Math.ceil(progress);
        Bitmap bmp = Bitmap.createBitmap(100, 1, Bitmap.Config.ARGB_8888);
        for(int i=0; i<100; i++) {
            if(i <= p)
                bmp.setPixel(i, 0, progressColor);
            else
                bmp.setPixel(i, 0, Color.LTGRAY);
        }
        return bmp;
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

                    tvProgress1.setText(String.format(Locale.getDefault(),
                            "Downloaded %d out of %d Pages (%d%%)",
                            downloadedPages, totalPages, chapterProgress));
                    ivProgress1.setImageBitmap(getProgressBitmap(progress[0]));
                    tvProgress2.setText(String.format(Locale.getDefault(),
                            "Downloading Page No. %d (%d%%)", currentPageNumber, pageProgress));
                    ivProgress2.setImageBitmap(getProgressBitmap(progress[1]));
                }

                @Override
                public void onFailedToDownloadChapter(FailureReason reason) {
                    if(chapter.equals(ivDownload.getTag()))
                        ivDownload.setImageResource(R.drawable.ic_download);
                    tvInfo.setText("Failed !");
                }
            });
        }
    }

}
