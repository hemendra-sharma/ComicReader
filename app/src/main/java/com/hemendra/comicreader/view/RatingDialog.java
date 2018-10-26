package com.hemendra.comicreader.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hemendra.comicreader.R;

public class RatingDialog {

    private Context context;

    private RelativeLayout mainRL;
    private Dialog dialog;

    public RatingDialog(Context context) {
        this.context = context;

        dialog = prepareLayout();
    }

    @SuppressLint("ClickableViewAccessibility")
    private Dialog prepareLayout() {
        View view = View.inflate(context, R.layout.dialog_rating, null);

        mainRL = view.findViewById(R.id.mainRL);

        ImageView ivStar1 = view.findViewById(R.id.ivStar1);
        ImageView ivStar2 = view.findViewById(R.id.ivStar2);
        ImageView ivStar3 = view.findViewById(R.id.ivStar3);
        ImageView ivStar4 = view.findViewById(R.id.ivStar4);
        ImageView ivStar5 = view.findViewById(R.id.ivStar5);

        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnCancel.setTransformationMethod(null);

        Dialog dialog = new Dialog(context, R.style.CategorySelectionDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(view);

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        View.OnClickListener starClickListener = v -> {
            switch (v.getTag().toString()) {
                case "1":
                    ivStar1.setImageResource(R.drawable.star_on);
                    ivStar2.setImageResource(R.drawable.star_off);
                    ivStar3.setImageResource(R.drawable.star_off);
                    ivStar4.setImageResource(R.drawable.star_off);
                    ivStar5.setImageResource(R.drawable.star_off);
                    break;
                case "2":
                    ivStar1.setImageResource(R.drawable.star_on);
                    ivStar2.setImageResource(R.drawable.star_on);
                    ivStar3.setImageResource(R.drawable.star_off);
                    ivStar4.setImageResource(R.drawable.star_off);
                    ivStar5.setImageResource(R.drawable.star_off);
                    break;
                case "3":
                    ivStar1.setImageResource(R.drawable.star_on);
                    ivStar2.setImageResource(R.drawable.star_on);
                    ivStar3.setImageResource(R.drawable.star_on);
                    ivStar4.setImageResource(R.drawable.star_off);
                    ivStar5.setImageResource(R.drawable.star_off);
                    break;
                case "4":
                    ivStar1.setImageResource(R.drawable.star_on);
                    ivStar2.setImageResource(R.drawable.star_on);
                    ivStar3.setImageResource(R.drawable.star_on);
                    ivStar4.setImageResource(R.drawable.star_on);
                    ivStar5.setImageResource(R.drawable.star_off);
                    break;
                case "5":
                    ivStar1.setImageResource(R.drawable.star_on);
                    ivStar2.setImageResource(R.drawable.star_on);
                    ivStar3.setImageResource(R.drawable.star_on);
                    ivStar4.setImageResource(R.drawable.star_on);
                    ivStar5.setImageResource(R.drawable.star_on);
                    break;
            }
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id="
                        +context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(intent.resolveActivity(context.getPackageManager()) != null)
                    context.startActivity(intent);

                if(dialog.isShowing())
                    dialog.dismiss();
            }, 1000);
        };

        ivStar1.setOnClickListener(starClickListener);
        ivStar2.setOnClickListener(starClickListener);
        ivStar3.setOnClickListener(starClickListener);
        ivStar4.setOnClickListener(starClickListener);
        ivStar5.setOnClickListener(starClickListener);

        View.OnTouchListener doFocus = (v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                v.requestFocus();
            return false;
        };

        ivStar1.setOnTouchListener(doFocus);
        ivStar2.setOnTouchListener(doFocus);
        ivStar3.setOnTouchListener(doFocus);
        ivStar4.setOnTouchListener(doFocus);
        ivStar5.setOnTouchListener(doFocus);

        return dialog;
    }

    public void show() {
        if(dialog != null) {
            dialog.show();

            mainRL.getLayoutParams().width
                    = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);
        }
    }
}
