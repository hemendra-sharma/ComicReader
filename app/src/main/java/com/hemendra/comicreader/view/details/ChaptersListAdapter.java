package com.hemendra.comicreader.view.details;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.presenter.ComicsPresenter;

import java.util.Locale;

public class ChaptersListAdapter extends RecyclerView.Adapter<ChaptersListAdapter.ChapterViewHolder>  {

    public class ChapterViewHolder extends RecyclerView.ViewHolder {

        ImageView ivDownload, ivProgress;
        TextView tvChapterName, tvProgress;
        Chapter chapter;
        Bitmap bmp;

        @SuppressLint("ClickableViewAccessibility")
        public ChapterViewHolder(@NonNull View itemView,
                                 OnChapterItemClickListener listener) {
            super(itemView);

            ivDownload = itemView.findViewById(R.id.ivDownload);
            tvChapterName = itemView.findViewById(R.id.tvChapterName);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            ivProgress = itemView.findViewById(R.id.ivProgress);

            ivDownload.setOnTouchListener((view, motionEvent) -> {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    view.requestFocus();
                return false;
            });

            itemView.setOnClickListener(view -> listener.onItemClicked(chapter));

            bmp = Bitmap.createBitmap(100, 1, Bitmap.Config.ARGB_8888);
        }
    }

    private Context context;
    private Comic comic;
    private ComicsPresenter presenter;
    private OnChapterItemClickListener listener;
    private int progressColor;
    private int textColor;
    private int darkGreen;
    private int darkRed;

    public ChaptersListAdapter(Context context, Comic comic,
                               ComicsPresenter presenter,
                               OnChapterItemClickListener listener) {
        this.context = context;
        this.comic = comic;
        this.presenter = presenter;
        this.listener = listener;
        this.progressColor = context.getResources().getColor(R.color.progressColor);
        this.textColor = context.getResources().getColor(R.color.textColor);
        this.darkGreen = context.getResources().getColor(R.color.textColorDarkGreen);
        this.darkRed = context.getResources().getColor(R.color.textColorDarkRed);
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chapter_list_item,
                viewGroup, false);
        return new ChapterViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder chapterViewHolder, int i) {
        chapterViewHolder.chapter = comic.chapters.get(i);

        chapterViewHolder.tvChapterName.setText(String.format(Locale.getDefault(),
                "%d. %s", comic.chapters.get(i).number, comic.chapters.get(i).title));

        chapterViewHolder.ivProgress.setImageBitmap(
                getProgressBitmap(chapterViewHolder.bmp,
                        comic.chapters.get(i).readingProgress,
                        comic.chapters.get(i).pages.size()));

        if(comic.chapters.get(i).pages.size() == 0) {
            chapterViewHolder.tvProgress.setText("Not Started Yet");
            chapterViewHolder.tvProgress.setTextColor(darkRed);
        } else if(comic.chapters.get(i).readingProgress+1
                >= comic.chapters.get(i).pages.size()) {
            chapterViewHolder.tvProgress.setText("Finished Reading");
            chapterViewHolder.tvProgress.setTextColor(darkGreen);
        } else {
            chapterViewHolder.tvProgress.setText(String.format(Locale.getDefault(),
                    "Reading Page No. %d out of %d Pages",
                    comic.chapters.get(i).readingProgress+1,
                    comic.chapters.get(i).pages.size()));
            chapterViewHolder.tvProgress.setTextColor(textColor);
        }

        if(presenter.isChapterOffline(comic.chapters.get(i))) {
            chapterViewHolder.ivDownload.setImageResource(R.drawable.ic_check);
            chapterViewHolder.ivDownload.setOnClickListener(null);
            chapterViewHolder.ivDownload.setTag(null);
        } else {
            chapterViewHolder.ivDownload.setTag(comic.chapters.get(i));
            chapterViewHolder.ivDownload.setImageResource(R.drawable.ic_download);
            chapterViewHolder.ivDownload.setOnClickListener(v->{
                if(v.getTag() != null) {
                    ChapterDownloaderDialog dialog = new ChapterDownloaderDialog(context,
                            (Chapter) v.getTag(), presenter, (ImageView) v);
                    dialog.show();
                }
            });
        }
    }

    private Bitmap getProgressBitmap(Bitmap bmp, int currentPage, int totalPages) {
        float progress = 0;
        if(totalPages > 0) {
            progress = ((float) (currentPage + 1) / (float) totalPages) * 100f;
        }
        int p = (int) Math.ceil(progress);
        for(int i=0; i<100; i++) {
            if(i <= p)
                bmp.setPixel(i, 0, progressColor);
            else
                bmp.setPixel(i, 0, Color.LTGRAY);
        }
        return bmp;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ChapterViewHolder chapterViewHolder) {
        chapterViewHolder.ivDownload.setTag(null);
        super.onViewDetachedFromWindow(chapterViewHolder);
    }

    @Override
    public int getItemCount() {
        return comic.chapters.size();
    }
}
