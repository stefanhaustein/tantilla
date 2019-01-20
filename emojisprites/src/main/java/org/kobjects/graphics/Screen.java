package org.kobjects.graphics;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class Screen extends ViewHolder<FrameLayout> implements Animated {
    public final Activity activity;
    /**
     * Multiply with scale to get from virtual coordinates to px, divide to get from px to
     * virtual coordinates.
     */
    public float scale;
    ImageView imageView;
    Bitmap bitmap;
    float bitmapScale;
    public Dpad dpad;
    Set<PositionedViewHolder<?>> widgets = Collections.newSetFromMap(new WeakHashMap<>());


    public Screen(@NonNull Activity activity) {
        super(new FrameLayout(activity));
        this.activity = activity;
        view.setClipChildren(false);

        int size = Dimensions.dpToPx(activity,200);

        bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmapScale = size / 200f;

        imageView = new ImageView(activity);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        dpad = new Dpad(this);

        clsImpl();

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                scale = Math.min(right - left, bottom - top) / 200f;
                dpad.requestSync();
            }
        });

        view.setFocusableInTouchMode(true);
    }

    public Pen createPen() {
       return new Pen(this);
    }


    public boolean dispatchKeyEvent(android.view.KeyEvent keyEvent) {
        System.out.println("KeyEvent: " + keyEvent);
        return false;
    }

    private void clsImpl() {
        view.removeAllViews();
        view.addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams dpadLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dpadLayoutParams.gravity = Gravity.BOTTOM;
        view.addView(dpad.view, dpadLayoutParams);
    }

    public void cls() {
        bitmap.eraseColor(0);
        dpad.setVisible(false);
        activity.runOnUiThread(() -> clsImpl());
    }

    @Override
    public float getWidthForAnchoring() {
        return -view.getWidth() / scale;
    }

    @Override
    public float getHeightForAnchoring() {
        return -view.getHeight() / scale;
    }

    @Override
    public void animate(float dt) {
        for(PositionedViewHolder<?> widget : widgets) {
            if (widget instanceof Animated) {
                ((Animated) widget).animate(dt);
            }
        }
    }
}
