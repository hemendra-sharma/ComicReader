package com.hemendra.comicreader.view.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hemendra.comicreader.R;
import com.hemendra.comicreader.model.data.Comic;
import com.hemendra.comicreader.model.source.comics.OnComicsLoadedListener;
import com.hemendra.comicreader.presenter.ComicsPresenter;

import java.util.ArrayList;

public class ComicsListAdapter extends RecyclerView.Adapter<ComicsListAdapter.ComicViewHolder> {

    public class ComicViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCover;
        TextView tvTitle, tvLastUpdated, tvCategories;
        Comic comic;

        public ComicViewHolder(@NonNull View itemView,
                               OnComicItemClickListener listener) {
            super(itemView);

            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLastUpdated = itemView.findViewById(R.id.tvLastUpdated);
            tvCategories = itemView.findViewById(R.id.tvCategories);

            itemView.setOnClickListener(view -> listener.onItemClicked(comic));
        }
    }

    private ArrayList<Comic> comics;
    private ComicsPresenter presenter;
    private OnComicItemClickListener onComicItemClickListener;

    public ComicsListAdapter(ArrayList<Comic> comics, ComicsPresenter presenter,
                             OnComicItemClickListener onComicItemClickListener) {
        this.comics = comics;
        this.presenter = presenter;
        this.onComicItemClickListener = onComicItemClickListener;
    }

    public void setComics(ArrayList<Comic> comics) {
        this.comics = comics;
    }

    public void clearItems() {
        this.comics.clear();
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
        comicViewHolder.comic = comics.get(position);
        comicViewHolder.tvTitle.setText(comics.get(position).title);
        comicViewHolder.tvCategories.setText(comics.get(position).getCategoriesString(2));
        comicViewHolder.tvLastUpdated.setText(comics.get(position).getLastUpdatedString());
        comicViewHolder.ivCover.setImageResource(R.drawable.no_cover);
        String url = comics.get(position).getImageUrl();
        if(url != null) {
            presenter.loadImage(url, comicViewHolder.ivCover);
        }
    }

    @Override
    public int getItemCount() {
        return comics.size();
    }

}
