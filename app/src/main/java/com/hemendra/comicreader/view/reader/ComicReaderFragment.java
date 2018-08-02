package com.hemendra.comicreader.view.reader;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.aphidmobile.flip.FlipViewController;
import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.presenter.ComicsPresenter;

public class ComicReaderFragment extends Fragment {

    private ComicsPresenter comicsPresenter;
    private Chapter chapter = null;
    private FlipViewController flipView;
    private ReaderAdapter adapter;
    private int currentPosition = 0;

    public static ComicReaderFragment getFragment(ComicsPresenter comicsPresenter) {
        ComicReaderFragment fragment = new ComicReaderFragment();
        fragment.comicsPresenter = comicsPresenter;
        return fragment;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
        this.currentPosition = 0;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comic_reader, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        view.setBackgroundColor(Color.BLACK);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        flipView = new FlipViewController(getContext(),
                FlipViewController.HORIZONTAL);
        flipView.setLayoutParams(params);
        flipView.setAnimationBitmapFormat(Bitmap.Config.RGB_565);
        ((RelativeLayout) view).addView(flipView);

        adapter = new ReaderAdapter(getContext(), chapter.pages,
                comicsPresenter, flipView);

        flipView.setAdapter(adapter);

        flipView.setOnViewFlipListener((view1, position) -> {
            currentPosition = position;
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        flipView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        flipView.onResume();
    }

    public void refreshFlipView() {
        if(currentPosition > 0)
            flipView.refreshPage(currentPosition-1);
        if(currentPosition < adapter.getCount())
            flipView.refreshPage(currentPosition);
        if(currentPosition < adapter.getCount()-1)
            flipView.refreshPage(currentPosition+1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
