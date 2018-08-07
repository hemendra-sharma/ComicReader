package com.hemendra.comicreader.view.details;

import android.annotation.SuppressLint;
import android.content.Context;
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

        ImageView ivDownload;
        TextView tvChapterName, tvChapterDate;
        Chapter chapter;

        @SuppressLint("ClickableViewAccessibility")
        public ChapterViewHolder(@NonNull View itemView,
                                 OnChapterItemClickListener listener) {
            super(itemView);

            ivDownload = itemView.findViewById(R.id.ivDownload);
            tvChapterName = itemView.findViewById(R.id.tvChapterName);
            tvChapterDate = itemView.findViewById(R.id.tvChapterDate);

            ivDownload.setOnTouchListener((view, motionEvent) -> {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    view.requestFocus();
                return false;
            });

            itemView.setOnClickListener(view -> listener.onItemClicked(chapter));
        }
    }

    private Context context;
    private Comic comic;
    private ComicsPresenter presenter;
    private OnChapterItemClickListener listener;

    public ChaptersListAdapter(Context context, Comic comic,
                               ComicsPresenter presenter,
                               OnChapterItemClickListener listener) {
        this.context = context;
        this.comic = comic;
        this.presenter = presenter;
        this.listener = listener;
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

        chapterViewHolder.tvChapterDate.setText(String.format(Locale.getDefault(),
                "Last Updated: %s", comic.chapters.get(i).getLastUpdatedString()));

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
