package com.hemendra.comicreader.view.details;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.presenter.ComicsPresenter;

import java.util.Locale;

@SuppressLint("ClickableViewAccessibility")
public class ComicDetailsFragment extends Fragment {

    private ComicsPresenter comicsPresenter;
    private Comic comic = null;

    private ImageView ivStar;

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
        RecyclerView recycler = view.findViewById(R.id.recycler);

        recycler.getLayoutParams().height = (int) (getResources()
                .getDisplayMetrics().heightPixels * 0.50);

        String url = comic.getImageUrl();
        if(url != null)
            comicsPresenter.loadImage(url, ivCover);

        if(comic.isFavorite)
            ivStar.setImageResource(R.drawable.star_on);
        else
            ivStar.setImageResource(R.drawable.star_off);

        ivStar.setOnTouchListener(doFocus);
        ivStar.setOnClickListener(onStarClicked);

        tvTitle.setText(comic.title);
        tvLastUpdated.setText(comic.getLastUpdatedString());
        tvCategories.setText(comic.getCategoriesString(10));
        tvDescription.setText(Html.fromHtml(comic.description, 0));
        tvReleasedYear.setText(String.format(Locale.getDefault(),
                "Released in %s", comic.released));
        tvHits.setText(String.format(Locale.getDefault(),
                "%s Hits", comic.hits));
        tvChapters.setText(String.format(Locale.getDefault(),
                "%s Chapters", comic.chapters.size()));

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setItemAnimator(new DefaultItemAnimator());

        ChaptersListAdapter adapter = new ChaptersListAdapter(comic,
                comicsPresenter, listener);
        recycler.setAdapter(adapter);

        recycler.smoothScrollToPosition(0);
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
            Toast.makeText(getContext(), "Removed From Favorites", Toast.LENGTH_SHORT).show();
        } else {
            ivStar.setImageResource(R.drawable.star_on);
            comicsPresenter.setComicFavorite(comic, true);
            Toast.makeText(getContext(), "Marked As Favorite", Toast.LENGTH_SHORT).show();
        }
    };

    private OnChapterItemClickListener listener = chapter -> comicsPresenter.loadPages(chapter);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
