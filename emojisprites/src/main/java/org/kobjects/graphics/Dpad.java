package org.kobjects.graphics;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Dpad extends LinearLayout {

    static LinearLayout.LayoutParams createLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        return layoutParams;
    }


    public ImageView up;
    public ImageView down;
    public ImageView left;
    public ImageView right;
    public ImageView fire;


    public Dpad(Context context) {
        super(context);


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
        updown.setOrientation(VERTICAL);
        updown.addView(up);
        updown.addView(down);

        addView(left, createLayoutParams());
        addView(updown, createLayoutParams());
        addView(right, createLayoutParams());

        addView(new View(context), new LinearLayout.LayoutParams(0, 0, 1));

        addView(fire, createLayoutParams());
    }



}
