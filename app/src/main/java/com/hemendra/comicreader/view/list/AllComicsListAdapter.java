package com.hemendra.comicreader.view.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.data.Comics;
import com.hemendra.comicreader.presenter.ComicsPresenter;

import java.util.ArrayList;

public class AllComicsListAdapter extends RecyclerView.Adapter<AllComicsListAdapter.ComicViewHolder> {

    public class ComicViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCover, ivStar;
        TextView tvTitle, tvLastUpdated, tvCategories;
        Comic comic;

        public ComicViewHolder(@NonNull View itemView,
                               OnComicItemClickListener listener) {
            super(itemView);

            ivCover = itemView.findViewById(R.id.ivCover);
            ivStar = itemView.findViewById(R.id.ivStar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLastUpdated = itemView.findViewById(R.id.tvLastUpdated);
            tvCategories = itemView.findViewById(R.id.tvCategories);

            itemView.setOnClickListener(view -> listener.onItemClicked(comic));
        }
    }

    private Comics comics;
    private Comics favoriteComics = new Comics();
    private ComicsPresenter presenter;
    private OnComicItemClickListener onComicItemClickListener;

    private int type = TYPE_ALL;

    public static final int TYPE_ALL = 0, TYPE_FAVORITES = 1;

    public AllComicsListAdapter(Comics comics, ComicsPresenter presenter,
                                OnComicItemClickListener onComicItemClickListener) {
        this.comics = comics;
        this.presenter = presenter;
        this.onComicItemClickListener = onComicItemClickListener;
        for(Comic comic : this.comics.comics) {
            if(comic.isFavorite)
                favoriteComics.comics.add(comic);
        }
    }

    public Comics getComics() {
        if(type == TYPE_ALL)
            return comics;
        else
            return favoriteComics;
    }

    public void setComics(Comics comics) {
        if(type == TYPE_ALL) {
            this.comics = comics;
            favoriteComics = new Comics();
            for (Comic comic : this.comics.comics) {
                if (comic.isFavorite)
                    favoriteComics.comics.add(comic);
            }
        } else {
            favoriteComics = comics;
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public void clearItems() {
        this.comics.comics.clear();
        this.favoriteComics.comics.clear();
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comic_list_item,
                viewGroup, false);
        return new ComicViewHolder(view, onComicItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder comicViewHolder, int position) {
        comicViewHolder.comic = getComic(position);
        comicViewHolder.tvTitle.setText(Html.fromHtml(getComic(position).title, 0));
        comicViewHolder.tvCategories.setText(getComic(position).getCategoriesString(2));
        comicViewHolder.tvLastUpdated.setText(getComic(position).getLastUpdatedString());
        comicViewHolder.ivCover.setImageResource(R.drawable.no_cover);
        if(getComic(position).isFavorite)
            comicViewHolder.ivStar.setVisibility(View.VISIBLE);
        else
            comicViewHolder.ivStar.setVisibility(View.GONE);
        String url = getComic(position).getImageUrl();
        if (url != null) {
            presenter.loadImage(url, comicViewHolder.ivCover);
        }
    }

    private Comic getComic(int position) {
        if(type == TYPE_ALL)
            return comics.comics.get(position);
        else
            return favoriteComics.comics.get(position);
    }

    @Override
    public int getItemCount() {
        if(type == TYPE_ALL)
            return comics.comics.size();
        else
            return favoriteComics.comics.size();
    }

}