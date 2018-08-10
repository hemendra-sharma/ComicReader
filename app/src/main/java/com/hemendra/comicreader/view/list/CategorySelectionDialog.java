package com.hemendra.comicreader.view.list;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hemendra.comicreader.R;

import java.util.ArrayList;
import java.util.Locale;

public class CategorySelectionDialog {

    private Context context;
    private ArrayList<String> categories;
    private ArrayList<Boolean> selection;
    private ArrayList<Boolean> savedSelection;
    private CategorySelectionListener listener;

    private RelativeLayout mainRL;
    private Dialog dialog;

    private Runnable selectAll = null;
    private Runnable selectNone = null;
    private Runnable restoreSelection = null;

    public CategorySelectionDialog(Context context,
                                   ArrayList<String> categories,
                                   CategorySelectionListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
        //
        this.selection = new ArrayList<>();
        this.savedSelection = new ArrayList<>();
        for(int i=0; i<this.categories.size(); i++) {
            selection.add(true);
            savedSelection.add(true);
        }
        //
        dialog = prepareLayout();
    }

    private Dialog prepareLayout() {
        View view = View.inflate(context, R.layout.dialog_category_selection, null);

        mainRL = view.findViewById(R.id.mainRL);
        LinearLayout ll = view.findViewById(R.id.ll);
        Button btn = view.findViewById(R.id.btn);
        Button btnAll = view.findViewById(R.id.btnAll);
        Button btnNone = view.findViewById(R.id.btnNone);

        btn.setTransformationMethod(null);
        btnAll.setTransformationMethod(null);
        btnNone.setTransformationMethod(null);

        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        for(int i=0; i<categories.size(); i++) {
            CheckBox cb = (CheckBox) View.inflate(context, R.layout.cb_spinner_drop_down_view, null);
            cb.setLayoutParams(cbParams);
            cb.setChecked(true);
            cb.setTag(i);
            cb.setText(String.format(Locale.getDefault(),
                    "   %s", getPersonifiedName(categories.get(i))));
            cb.setOnCheckedChangeListener((compoundButton, checked) ->
                    selection.set(((Integer)compoundButton.getTag()), checked));
            ll.addView(cb);
        }

        Dialog dialog = new Dialog(context, R.style.CategorySelectionDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(view);

        btn.setOnClickListener(v->{
            if(callListener())
                dialog.dismiss();
        });

        selectAll = ()->{
            for(int i=0; i<ll.getChildCount(); i++)
                ((CheckBox)ll.getChildAt(i)).setChecked(true);
        };

        selectNone = ()->{
            for(int i=0; i<ll.getChildCount(); i++)
                ((CheckBox)ll.getChildAt(i)).setChecked(false);
        };

        restoreSelection = ()->{
            for(int i=0; i<selection.size(); i++) {
                ((CheckBox)ll.getChildAt(i)).setChecked(savedSelection.get(i));
            }
        };

        btnAll.setOnClickListener(v-> selectAll());

        btnNone.setOnClickListener(v-> selectNone());

        dialog.setOnCancelListener(dialogInterface -> restoreSelection.run());

        return dialog;
    }

    private String getPersonifiedName(String str) {
        if(str.length() > 1)
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
        else
            str = str.toUpperCase();
        return str;
    }

    public void selectAll() {
        selectAll.run();
        if(callListener())
            dialog.dismiss();
    }

    public void selectNone() {
        selectNone.run();
    }

    public int getTotalCategoriesCount() {
        return categories.size();
    }

    private boolean callListener() {
        if(listener != null) {
            ArrayList<String> selectedCategories = new ArrayList<>();
            for(int i=0; i<selection.size(); i++) {
                savedSelection.set(i, selection.get(i));
                if(selection.get(i)) {
                    selectedCategories.add(categories.get(i));
                }
            }
            if(selectedCategories.size() > 0) {
                listener.onSelectionChanged(selectedCategories);
                return true;
            } else {
                Toast.makeText(context, "Select at least 1 category", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    public void show() {
        if(dialog != null) {
            for(int i=0; i<selection.size(); i++) {
                savedSelection.set(i, selection.get(i));
            }

            dialog.show();

            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);
            int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.50);

            mainRL.getLayoutParams().width = width;
            mainRL.getLayoutParams().height = height;
        }
    }
}
