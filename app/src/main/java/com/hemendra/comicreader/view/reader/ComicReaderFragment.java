/*
 * Copyright (c) 2018 Hemendra Sharma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hemendra.comicreader.view.reader;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
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

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class ComicReaderFragment extends Fragment {

    private ComicsPresenter comicsPresenter;
    private Chapter chapter = null;
    private FlipViewController flipView;
    private ReaderAdapter adapter;
    private TextView tvPageProgress, tvTitle;
    private ImageView ivPageProgress;
    public int currentPosition = 0;

    private boolean isTutorialShowing = false;
    private static final String TUTORIAL_ID = "reader_screen_tutorial";

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
        if(chapter == null)
            return;

        view.setBackgroundColor(Color.BLACK);

        RelativeLayout container = view.findViewById(R.id.container);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvPageProgress = view.findViewById(R.id.tvPageProgress);
        ivPageProgress = view.findViewById(R.id.ivPageProgress);
        View vTutorialTarget = view.findViewById(R.id.vTutorialTarget);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        flipView = new FlipViewController(getContext(),
                FlipViewController.HORIZONTAL);
        flipView.setLayoutParams(params);
        flipView.setAnimationBitmapFormat(Bitmap.Config.RGB_565);
        flipView.setOverFlipEnabled(false);
        container.addView(flipView);

        Chapter ch = comicsPresenter.getOfflineChapter(chapter);
        if(ch == null) {
            ch = chapter;
        }

        adapter = new ReaderAdapter(getActivity(), this, ch.pages,
                comicsPresenter, flipView, overlaysVisibility,
                comicsPresenter.getNextChapterFrom(chapter));
        flipView.setAdapter(adapter);

        refreshUI();

        showTutorial(vTutorialTarget);
    }

    public boolean onBackPressed() {
        return isTutorialShowing;
    }

    public void refreshUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvTitle.setText(Html.fromHtml(chapter.title, 0));
        } else {
            tvTitle.setText(Html.fromHtml(chapter.title));
        }

        if(chapter.readingProgress > 0) {
            flipView.setSelection(chapter.readingProgress);
            currentPosition = chapter.readingProgress;
            updateProgress();
        }

        flipView.setOnViewFlipListener((view1, position) -> {
            if(position > chapter.readingProgress) {
                chapter.readingProgress = position;
            }
            tvTitle.setVisibility(View.GONE);
            currentPosition = position;
            updateProgress();
        });

        updateProgress();
    }

    private Runnable overlaysVisibility = () -> {
        if(tvTitle.getVisibility() == View.VISIBLE)
            tvTitle.setVisibility(View.GONE);
        else
            tvTitle.setVisibility(View.VISIBLE);
    };

    public void updateProgress() {
        if(adapter.getItemViewType(currentPosition) == ReaderAdapter.TYPE_NEXT_CHAPTER) {
            tvPageProgress.setVisibility(View.INVISIBLE);
            ivPageProgress.setVisibility(View.INVISIBLE);
        } else {
            tvPageProgress.setVisibility(View.VISIBLE);
            ivPageProgress.setVisibility(View.VISIBLE);
            int totalPages = adapter.getCount();
            int currentPage = currentPosition + 1;
            int percent = (int) (((float) currentPage / (float) totalPages) * 100f);
            Bitmap bmp = Bitmap.createBitmap(100, 1, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < 100; i++) {
                if (i <= percent)
                    bmp.setPixel(i, 0, Color.DKGRAY);
                else
                    bmp.setPixel(i, 0, Color.BLACK);
            }
            tvPageProgress.setText(getString(R.string.page_d_d, currentPage, totalPages));
            ivPageProgress.setImageBitmap(bmp);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        flipView.onPause();
        updateChapterProgress();
    }

    public void updateChapterProgress() {
        if(chapter.readingProgress > 0) {
            comicsPresenter.updateChapter(chapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        flipView.onResume();
        refreshFlipView();
    }

    public void refreshFlipView() {
        if(adapter.isZoomed)
            return;
        if(currentPosition > 0)
            flipView.refreshPage(currentPosition-1);
        if(currentPosition < adapter.getCount())
            flipView.refreshPage(currentPosition);
        if(currentPosition < adapter.getCount()-1)
            flipView.refreshPage(currentPosition+1);
    }

    private void showTutorial(View v) {
        if(getActivity() == null)
            return;

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(),
                TUTORIAL_ID);
        sequence.setConfig(config);

        sequence.addSequenceItem(v,
                getString(R.string.reader_tutorial_1),
                getString(R.string.next));

        sequence.addSequenceItem(v,
                getString(R.string.reader_tutorial_2),
                getString(R.string.next));

        sequence.addSequenceItem(v,
                getString(R.string.reader_tutorial_3),
                getString(R.string.got_it));

        sequence.setOnItemShownListener((materialShowcaseView, i) -> {
            if(i == 0)
                isTutorialShowing = true;
        });

        sequence.setOnItemDismissedListener((materialShowcaseView, i) -> {
            if(i == 2)
                isTutorialShowing = false;
        });

        sequence.start();
    }

}
