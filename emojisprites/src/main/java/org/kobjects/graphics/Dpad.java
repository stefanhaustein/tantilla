package org.kobjects.graphics;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Dpad  {

    static final int BUTTON_SIZE = 30;

    static LinearLayout.LayoutParams createLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        return layoutParams;
    }

    private final Screen screen;
    final LinearLayout view;
    private boolean syncRequested;
    private boolean visible;

    public ImageView up;
    public ImageView down;
    public ImageView left;
    public ImageView right;
    public ImageView fire;


    public Dpad(Screen screen) {
        this.screen = screen;
        Context context = screen.activity;
        this.view = new LinearLayout(context);

        left = new ImageView(context);
        left.setImageResource(R.drawable.baseline_arrow_back_24);
        //left.setImageDrawable(Emojis.getDrawable(context, "◀️"));
        up = new ImageView(context);
        up.setImageResource(R.drawable.baseline_arrow_upward_24);
        // up.setImageDrawable(Emojis.getDrawable(context, "\uD83D\uDD3C️️️"));
        down = new ImageView(context);
        down.setImageResource(R.drawable.baseline_arrow_downward_24);
//        down.setImageDrawable(Emojis.getDrawable(context, "\uD83D\uDD3D"));
        right = new ImageView(context);
        right.setImageResource(R.drawable.baseline_arrow_forward_24);

  //      right.setImageDrawable(Emojis.getDrawable(context, "▶️"));
        fire = new ImageView(context);
        fire.setImageResource(R.drawable.baseline_adjust_24);
        // fire.setImageDrawable(Emojis.getDrawable(context, "\u23FA️"));

        LinearLayout updown = new LinearLayout(context);
        updown.setOrientation(LinearLayout.VERTICAL);
        updown.addView(up);
        updown.addView(down);

        view.addView(left, createLayoutParams());
        view.addView(updown, createLayoutParams());
        view.addView(right, createLayoutParams());

        view.addView(new View(context), new LinearLayout.LayoutParams(0, 0, 1));

        view.addView(fire, createLayoutParams());

        view.setVisibility(View.GONE);
    }

    public boolean setVisible(boolean visible) {
        if (this.visible == visible) {
            return false;
        }
        this.visible = visible;
        requestSync();
        return true;
    }


    void adjustSize(ImageView view) {
        // TODO: Take distortion into account.
        int imageSize = Math.round(Math.min(screen.scale * BUTTON_SIZE, Dimensions.dpToPx(view.getContext(), 48)));
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
       // view.setBackgroundColor(0x88000000);
        if (layoutParams.width != imageSize || layoutParams.height != imageSize) {
            layoutParams.width = imageSize;
            layoutParams.height = imageSize;
            view.setPadding(imageSize/4, imageSize/4, imageSize/4, imageSize/4);
            view.setLayoutParams(layoutParams);
            view.requestLayout();
        }
    }

    void requestSync() {
        if (!syncRequested) {
            syncRequested = true;
            screen.activity.runOnUiThread(() -> syncUi());
        }

    }

    public boolean getVisible() {
        return visible;
    }


    void syncUi() {
        syncRequested = false;
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        adjustSize(left);
        adjustSize(up);
        adjustSize(down);
        adjustSize(right);
        adjustSize(fire);


    }
}
