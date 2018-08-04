package com.hemendra.comicreader.view.reader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.aphidmobile.flip.FlipViewController;
import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Page;
import com.hemendra.comicreader.presenter.ComicsPresenter;

import java.util.ArrayList;

public class ReaderAdapter extends ArrayAdapter<Page> {

    private ComicsPresenter presenter;
    private ArrayList<Page> pages;
    private FlipViewController flipView;

    public ReaderAdapter(Context context,
                         ArrayList<Page> pages, ComicsPresenter presenter,
                         FlipViewController flipView) {
        super(context, 0);
        this.pages = pages;
        this.presenter = presenter;
        this.flipView = flipView;
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

        TouchImageView iv = new TouchImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setLayoutParams(params);
        iv.setMaxZoom(3);
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

        iv.setOnClickListener(v->setImage(pages.get((Integer)v.getTag()), ((TouchImageView)v)));

        return iv;
    }

    private void setImage(Page page, TouchImageView iv) {
        String url = page.getImageUrl();
        if(url != null)
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
