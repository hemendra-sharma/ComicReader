package com.hemendra.comicreader.view.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.presenter.ComicsPresenter;
import com.hemendra.comicreader.view.list.AllComicsListFragment;

import java.util.Locale;

public class ComicDetailsFragment extends Fragment {

    private ComicsPresenter comicsPresenter;
    private Comic comic = null;

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
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvLastUpdated = view.findViewById(R.id.tvLastUpdated);
        TextView tvCategories = view.findViewById(R.id.tvCategories);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvReleasedYear = view.findViewById(R.id.tvReleasedYear);
        TextView tvHits = view.findViewById(R.id.tvHits);
        TextView tvChapters = view.findViewById(R.id.tvChapters);
        LinearLayout llChapters = view.findViewById(R.id.llChapters);

        String url = comic.getImageUrl();
        if(url != null)
            comicsPresenter.loadImage(url, ivCover);

        tvTitle.setText(comic.title);
        tvLastUpdated.setText(comic.getLastUpdatedString());
        tvCategories.setText(comic.getCategoriesString(10));
        tvDescription.setText(Html.fromHtml(comic.description));
        tvReleasedYear.setText(String.format(Locale.getDefault(),
                "Released in %s", comic.released));
        tvHits.setText(String.format(Locale.getDefault(),
                "%s Hits", comic.hits));
        tvChapters.setText(String.format(Locale.getDefault(),
                "%s Chapters", comic.chapters.size()));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(10, 20, 10, 20);

        for(Chapter chapter : comic.chapters) {
            View chapterView = View.inflate(getContext(), R.layout.chapter_list_item, null);

            TextView tvChapterName = chapterView.findViewById(R.id.tvChapterName);
            tvChapterName.setText(String.format(Locale.getDefault(),
                    "%d. %s", chapter.number, chapter.title));

            TextView tvChapterDate = chapterView.findViewById(R.id.tvChapterDate);
            tvChapterDate.setText(String.format(Locale.getDefault(),
                    "Updated: %s", chapter.getLastUpdatedString()));

            chapterView.setOnClickListener(v-> comicsPresenter.loadChapterPages(chapter));

            llChapters.addView(chapterView, params);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
