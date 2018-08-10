package com.hemendra.comicreader.view.reader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.flip.FlipViewController;
import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.model.data.Page;
import com.hemendra.comicreader.model.source.FailureReason;
import com.hemendra.comicreader.model.source.comics.ComicsDataSource;
import com.hemendra.comicreader.model.source.comics.IComicsDataSourceListener;
import com.hemendra.comicreader.presenter.ComicsPresenter;

import java.util.ArrayList;
import java.util.Locale;

public class ReaderAdapter extends ArrayAdapter<Page> {

    private ComicReaderFragment fragment;
    private ComicsPresenter presenter;
    private ArrayList<Page> pages;
    private FlipViewController flipView;
    private Runnable onClicked;
    private Chapter nextChapter;

    public ReaderAdapter(Context context, ComicReaderFragment fragment,
                         ArrayList<Page> pages, ComicsPresenter presenter,
                         FlipViewController flipView,
                         Runnable onClicked,
                         Chapter nextChapter) {
        super(context, 0);
        this.fragment = fragment;
        this.pages = pages;
        this.presenter = presenter;
        this.flipView = flipView;
        this.onClicked = onClicked;
        this.nextChapter = nextChapter;
        if(this.nextChapter != null
                && !this.pages.get(this.pages.size()-1).id.equals("next_chapter")) {
            Page p = new Page(99999, "next_chapter");
            this.pages.add(p);
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public Page getItem(int i) {
        return pages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.MATCH_PARENT);

        if(pages.get(i).id.equals("next_chapter")) {
            View v = View.inflate(getContext(), R.layout.next_chapter_page, null);
            TextView tvNextChapterInfo = v.findViewById(R.id.tvNextChapterInfo);
            Button btnStartReading = v.findViewById(R.id.btnStartReading);
            String html = "<small>Chapter Complete</small>" +
                    "<br><br>-: Next Chapter :-" +
                    "<br><br><big><b>" + nextChapter.title + "</b></big>";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvNextChapterInfo.setText(Html.fromHtml(html, 0));
            } else {
                tvNextChapterInfo.setText(Html.fromHtml(html));
            }
            btnStartReading.setOnClickListener(btn->{
                fragment.updateChapterProgress();
                presenter.loadPages(nextChapter, nextChapterLoadingListener);
            });
            return v;
        } else {
            TouchImageView iv = new TouchImageView(getContext());
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setLayoutParams(params);
            iv.setMaxZoom(3);
            iv.setMediumScale(2);
            iv.setBackgroundColor(Color.TRANSPARENT);
            iv.setImageResource(R.drawable.loading_text);
            iv.setTag(i);

            iv.setOnTouchListener((v, event) -> {
                v.invalidate();
                TouchImageView img = (TouchImageView)v;
                if(img.isZoomed()) {
                    flipView.setFlipByTouchEnabled(false);
                } else {
                    flipView.setFlipByTouchEnabled(true);
                }
                return false;
            });

            setImage(pages.get(i), iv);

            iv.setOnClickListener(v->{
                onClicked.run();
                if(((Integer) v.getTag()) >= 0) {
                    setImage(pages.get((Integer) v.getTag()), ((TouchImageView) v));
                } else if(((TouchImageView)v).isZoomed()) {
                    ((TouchImageView)v).resetZoomSmooth();
                }
            });

            return iv;
        }
    }

    private IComicsDataSourceListener nextChapterLoadingListener = new IComicsDataSourceListener() {
        @Override
        public void onStartedLoadingComics() {}
        @Override
        public void onComicsLoaded(@NonNull Comics comics, @NonNull ComicsDataSource.SourceType sourceType) { }
        @Override
        public void onFailedToLoadComics(@NonNull FailureReason reason) { }
        @Override
        public void onStartedLoadingComicDetails() { }
        @Override
        public void onComicDetailsLoaded(Comic comic) { }
        @Override
        public void onFailedToLoadComicDetails(FailureReason reason) { }

        @Override
        public void onStartedLoadingPages() {
            presenter.showProgress();
        }

        @Override
        public void onPagesLoaded(Chapter chapter) {
            fragment.setChapter(chapter);
            Chapter ch = presenter.getOfflineChapter(chapter);
            if(ch == null) {
                ch = chapter;
            }
            pages = ch.pages;
            nextChapter = presenter.getNextChapterFrom(chapter);
            if(nextChapter != null) {
                Page p = new Page(99999, "next_chapter");
                pages.add(p);
            }
            notifyDataSetChanged();
            fragment.refreshUI();
            if(chapter.readingProgress == 0)
                flipView.setSelection(0);
            presenter.hideProgress();
        }

        @Override
        public void onFailedToLoadPages(FailureReason reason) {
            presenter.hideProgress();
            Toast.makeText(getContext(), "Failed to Load Next Chapter !", Toast.LENGTH_SHORT).show();
        }
    };

    private void setImage(Page page, TouchImageView iv) {
        if(page.rawImageData != null && page.rawImageData.length > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inSampleSize = 1;
            Bitmap bmp = BitmapFactory.decodeByteArray(page.rawImageData,
                    0, page.rawImageData.length, options);
            if(bmp != null) {
                iv.setImageBitmap(bmp);
                iv.setTag(-1);
                return;
            }
        }

        String url = page.getImageUrl();
        if (url != null)
            presenter.loadPage(url, iv);
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
