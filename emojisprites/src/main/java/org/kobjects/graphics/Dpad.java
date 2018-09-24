package org.kobjects.graphics;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Dpad extends ViewHolder<LinearLayout> {

    static LinearLayout.LayoutParams createLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        return layoutParams;
    }

    private boolean syncRequested;
    private boolean visible;

    public ImageView up;
    public ImageView down;
    public ImageView left;
    public ImageView right;
    public ImageView fire;


    public Dpad(Viewport viewport) {
        super(viewport, new LinearLayout(viewport.getContext()));
        Context context = viewport.getContext();


        left = new ImageView(context);
        left.setImageDrawable(Emojis.getDrawable(context, "◀️"));
        up = new ImageView(context);
        up.setImageDrawable(Emojis.getDrawable(context, "\uD83D\uDD3C️️️"));
        down = new ImageView(context);
        down.setImageDrawable(Emojis.getDrawable(context, "\uD83D\uDD3D"));
        right = new ImageView(context);
        right.setImageDrawable(Emojis.getDrawable(context, "▶️"));
        fire = new ImageView(context);
        fire.setImageDrawable(Emojis.getDrawable(context, "\u23FA️"));

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


    void requestSync() {
        if (!syncRequested) {
            syncRequested = true;
            viewport.activity.runOnUiThread(this);
        }
    }

    public boolean getVisible() {
        return visible;
    }


    @Override
    public void run() {
        syncRequested = false;
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
