package com.hemendra.comicreader.view.list;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.hemendra.comicreader.R;

import java.util.ArrayList;
import java.util.Locale;

public class CategoriesAdapter implements SpinnerAdapter {

    private Context context;
    private ArrayList<String> categories;
    private ArrayList<Boolean> selection;
    private CategorySelectionListener listener;
    private TextView tv = null;

    public CategoriesAdapter(Context context, ArrayList<String> categories,
                             CategorySelectionListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
        this.selection = new ArrayList<>();
        for(int i=0; i<this.categories.size(); i++)
            selection.add(true);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Spinner.LayoutParams params = new Spinner.LayoutParams(
                Spinner.LayoutParams.MATCH_PARENT,
                Spinner.LayoutParams.WRAP_CONTENT);

        tv = (TextView) View.inflate(context, R.layout.tv_spinner_drop_down_view, null);
        tv.setLayoutParams(params);
        setSelectedCount();

        return tv;
    }

    private void setSelectedCount() {
        if(tv != null) {
            int count = 0;
            for(Boolean b : selection) {
                if(b) count++;
            }
            tv.setText(String.format(Locale.getDefault(), "%d Selected", count));
        }
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup viewGroup) {
        Spinner.LayoutParams params = new Spinner.LayoutParams(
                Spinner.LayoutParams.MATCH_PARENT,
                Spinner.LayoutParams.WRAP_CONTENT);

        CheckBox cb = (CheckBox) View.inflate(context, R.layout.cb_spinner_drop_down_view, null);
        cb.setLayoutParams(params);
        cb.setText(String.format(Locale.getDefault(), "   %s", categories.get(position)));
        cb.setSelected(selection.get(position));
        cb.setTag(position);
        cb.setOnCheckedChangeListener((compoundButton, checked) -> {
            selection.set(((Integer)compoundButton.getTag()), checked);
            setSelectedCount();
            callListener();
        });

        return cb;
    }

    private void callListener() {
        if(listener != null) {
            ArrayList<String> selectedCategories = new ArrayList<>();
            for(int i=0; i<selection.size(); i++) {
                if(selection.get(i)) {
                    selectedCategories.add(categories.get(i));
                }
            }
            listener.onSelectionChanged(selectedCategories);
        }
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public String getItem(int i) {
        return categories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
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
        return categories.size() == 0;
    }
}
