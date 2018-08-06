package com.hemendra.comicreader.view.reader;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphidmobile.flip.FlipViewController;
import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.presenter.ComicsPresenter;

import java.util.Locale;

public class ComicReaderFragment extends Fragment {

    private ComicsPresenter comicsPresenter;
    private Chapter chapter = null;
    private FlipViewController flipView;
    private ReaderAdapter adapter;
    private TextView tvPageProgress;
    private ImageView ivPageProgress;
    private int currentPosition = 0;
    private int progressColor = Color.DKGRAY;

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

        RelativeLayout container = view.findViewById(R.id.container);
        tvPageProgress = view.findViewById(R.id.tvPageProgress);
        ivPageProgress = view.findViewById(R.id.ivPageProgress);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        flipView = new FlipViewController(getContext(),
                FlipViewController.HORIZONTAL);
        flipView.setLayoutParams(params);
        flipView.setAnimationBitmapFormat(Bitmap.Config.RGB_565);
        flipView.setOverFlipEnabled(false);
        container.addView(flipView);

        adapter = new ReaderAdapter(getContext(), chapter.pages,
                comicsPresenter, flipView);

        flipView.setAdapter(adapter);

        flipView.setOnViewFlipListener((view1, position) -> {
            currentPosition = position;
            updateProgress();
        });

        updateProgress();
    }

    private void updateProgress() {
        int totalPages = adapter.getCount();
        int currentPage = currentPosition+1;
        int percent = (int)(((float) currentPage / (float) totalPages) * 100f);
        Bitmap bmp = Bitmap.createBitmap(100, 1, Bitmap.Config.ARGB_8888);
        for(int i=0; i<100; i++) {
            if(i <= percent)
                bmp.setPixel(i, 0, progressColor);
            else
                bmp.setPixel(i, 0, Color.BLACK);
        }
        tvPageProgress.setText(String.format(Locale.getDefault(),
                "Page %d/%d", currentPage, totalPages));
        ivPageProgress.setImageBitmap(bmp);
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
        refreshFlipView();
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
