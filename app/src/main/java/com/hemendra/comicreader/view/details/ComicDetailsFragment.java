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

package com.hemendra.comicreader.view.details;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.presenter.ComicsPresenter;
import com.hemendra.comicreader.view.ImageAndViewHolder;

import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

@SuppressLint("ClickableViewAccessibility")
public class ComicDetailsFragment extends Fragment {

    private ComicsPresenter comicsPresenter;
    private Comic comic = null;

    private RecyclerView recycler;
    private RelativeLayout rlDetails;
    private Button btnStartReading1;
    private ImageView ivStar;
    private View v1;

    private ChaptersListAdapter adapter = null;

    private boolean isTutorialShowing = false;
    private static final String TUTORIAL_ID = "details_tutorial",
                CHAPTER_DOWNLOAD_TUTORIAL_ID = "chapter_download_tutorial";

    public static ComicDetailsFragment getFragment(ComicsPresenter comicsPresenter) {
        ComicDetailsFragment fragment = new ComicDetailsFragment();
        fragment.comicsPresenter = comicsPresenter;
        return fragment;
    }

    public void setComic(Comic comic) {
        this.comic = comic;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comic_details, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ImageView ivCover = view.findViewById(R.id.ivCover);
        ivStar = view.findViewById(R.id.ivStar);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvLastUpdated = view.findViewById(R.id.tvLastUpdated);
        TextView tvCategories = view.findViewById(R.id.tvCategories);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvReleasedYear = view.findViewById(R.id.tvReleasedYear);
        TextView tvHits = view.findViewById(R.id.tvHits);
        TextView tvChapters = view.findViewById(R.id.tvChapters);
        recycler = view.findViewById(R.id.recycler);
        btnStartReading1 = view.findViewById(R.id.btnStartReading1);
        Button btnStartReading2 = view.findViewById(R.id.btnStartReading2);
        rlDetails = view.findViewById(R.id.rlDetails);
        v1 = view.findViewById(R.id.v1);

        String url = comic.getImageUrl();
        if(url != null)
            comicsPresenter.loadImage(url, new ImageAndViewHolder(ivCover));

        if(comic.isFavorite)
            ivStar.setImageResource(R.drawable.star_on);
        else
            ivStar.setImageResource(R.drawable.star_off);

        ivStar.setOnTouchListener(doFocus);
        ivStar.setOnClickListener(onStarClicked);

        btnStartReading1.setTransformationMethod(null);
        btnStartReading2.setTransformationMethod(null);

        tvTitle.setText(comic.title);
        tvLastUpdated.setText(comic.getLastUpdatedString());
        tvCategories.setText(comic.getCategoriesString(10));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvDescription.setText(Html.fromHtml(comic.description, 0));
        } else {
            tvDescription.setText(Html.fromHtml(comic.description));
        }
        tvReleasedYear.setText(getString(R.string.released_in_s, comic.released));
        tvHits.setText(getString(R.string._d_hits, comic.hits));
        tvChapters.setText(getString(R.string._d_chapters, comic.chapters.size()));

        recycler.setItemAnimator(new DefaultItemAnimator());

        if(getActivity() != null) {
            adapter = new ChaptersListAdapter(getActivity(), comic, comicsPresenter, listener);
            recycler.setAdapter(adapter);
        }

        int lastReadIndex = 0;
        for(int i=comic.chapters.size()-1; i>=0; i--) {
            if(comic.chapters.get(i).readingProgress > 0) {
                lastReadIndex = i;
                break;
            }
        }

        if(recycler.getLayoutManager() != null)
            recycler.getLayoutManager().scrollToPosition(lastReadIndex);

        btnStartReading1.setOnClickListener(onStartReadingClicked);
        btnStartReading2.setOnClickListener(onStartReadingClicked);

        showTutorial();
    }

    public Chapter getNextChapterFrom(Chapter ch) {
        for(int i=0; i<comic.chapters.size(); i++) {
            if(comic.chapters.get(i).id.equals(ch.id)) {
                if(i+1 < comic.chapters.size()) {
                    return comic.chapters.get(i+1);
                }
                break;
            }
        }
        return null;
    }

    private View.OnClickListener onStartReadingClicked = v->{
        if(adapter != null && adapter.getItemCount() > 0) {
            recycler.setVisibility(View.VISIBLE);
            rlDetails.setVisibility(View.GONE);
            showChapterDownloadTutorial();
        } else {
            Toast.makeText(getContext(), R.string.no_chapters_available_right_now, Toast.LENGTH_SHORT).show();
        }
    };

    public boolean onBackPressed() {
        if(isTutorialShowing) {
            return true;
        } else if(recycler.getVisibility() == View.VISIBLE) {
            recycler.setVisibility(View.GONE);
            rlDetails.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private View.OnTouchListener doFocus = (view, motionEvent) -> {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            view.requestFocus();
        return false;
    };

    private View.OnClickListener onStarClicked = v -> {
        if(comic.isFavorite) {
            ivStar.setImageResource(R.drawable.star_off);
            comicsPresenter.setComicFavorite(comic, false);
            Toast.makeText(getContext(), R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
        } else {
            ivStar.setImageResource(R.drawable.star_on);
            comicsPresenter.setComicFavorite(comic, true);
            Toast.makeText(getContext(), R.string.marked_as_favorite, Toast.LENGTH_SHORT).show();
        }
    };

    private OnChapterItemClickListener listener = chapter -> comicsPresenter.loadPages(chapter);

    private void showTutorial() {
        if(getActivity() == null)
            return;

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), TUTORIAL_ID);
        sequence.setConfig(config);

        sequence.addSequenceItem(ivStar,
                getString(R.string.details_tutorial_1), getString(R.string.next));

        sequence.addSequenceItem(btnStartReading1,
                getString(R.string.details_tutorial_2), getString(R.string.got_it));

        sequence.setOnItemShownListener((materialShowcaseView, i) -> {
            if(i == 0)
                isTutorialShowing = true;
        });

        sequence.setOnItemDismissedListener((materialShowcaseView, i) -> {
            if(i == 1)
                isTutorialShowing = false;
        });

        sequence.start();
    }

    private void showChapterDownloadTutorial() {
        new MaterialShowcaseView.Builder(getActivity())
                .setTarget(v1)
                .setDismissText(getString(R.string.got_it))
                .setContentText(R.string.chapter_buffer_tutorial)
                .setDelay(500)
                .singleUse(CHAPTER_DOWNLOAD_TUTORIAL_ID)
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {
                        isTutorialShowing = true;
                    }
                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                        isTutorialShowing = false;
                    }
                })
                .show();
    }

    public void refreshChaptersList() {
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

}
