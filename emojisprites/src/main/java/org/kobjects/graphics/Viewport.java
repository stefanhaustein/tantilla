package org.kobjects.graphics;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class Viewport extends FrameLayout {
    public final Activity activity;
    public float scale;
    ImageView imageView;
    Bitmap bitmap;
    float bitmapScale;
    public Dpad dpad;

    public Viewport(@NonNull Activity activity) {
        super(activity);
        this.activity = activity;
        setClipChildren(false);

        int size = Dimensions.dpToPx(activity,200);

        bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmapScale = size / 200f;

        imageView = new ImageView(activity);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        dpad = new Dpad(this);

        cls();

        addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                scale = Math.min(right - left, bottom - top) / 200f;
                dpad.requestSync();
            }
        });


        //setClickable(true);
        //setFocusable(FOCUSABLE);
        setFocusableInTouchMode(true);
    }

    public Pen createPen() {
       return new Pen(this);
    }


    public boolean dispatchKeyEvent(android.view.KeyEvent keyEvent) {
        System.out.println("KeyEvent: " + keyEvent);
        return false;
    }

    public void cls() {
        removeAllViews();
        bitmap.eraseColor(0);
        addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams dpadLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dpadLayoutParams.gravity = Gravity.BOTTOM;
        addView(dpad.getView(), dpadLayoutParams);
        dpad.setVisible(false);
    }
}
