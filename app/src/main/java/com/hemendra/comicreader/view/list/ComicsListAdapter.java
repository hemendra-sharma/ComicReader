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

import java.util.ArrayList;

public class ComicsListAdapter extends RecyclerView.Adapter<ComicsListAdapter.ComicViewHolder> {

    public class ComicViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCover;
        TextView tvTitle, tvLastUpdated, tvCategories;

        public ComicViewHolder(@NonNull View itemView) {
            super(itemView);

            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLastUpdated = itemView.findViewById(R.id.tvLastUpdated);
            tvCategories = itemView.findViewById(R.id.tvCategories);
        }
    }

    private ArrayList<Comic> comics;

    public ComicsListAdapter(ArrayList<Comic> comics) {
        this.comics = comics;
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comic_list_item,
                viewGroup, false);
        return new ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder comicViewHolder, int position) {
        comicViewHolder.tvTitle.setText(comics.get(position).title);
        comicViewHolder.tvLastUpdated.setText(comics.get(position).getLastUpdatedString());
        comicViewHolder.tvCategories.setText(comics.get(position).getCategoriesString(5));
    }

    @Override
    public int getItemCount() {
        return comics.size();
    }

}
