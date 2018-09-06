package org.kobjects.graphics;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class Viewport extends FrameLayout {
    public final Activity activity;
    public float scale;
    ImageView imageView;
    Bitmap bitmap;

    public Viewport(@NonNull Activity activity) {
        super(activity);
        this.activity = activity;
        setClipChildren(false);

        bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

        imageView = new ImageView(activity);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        clear();

        addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                scale = Math.min(right - left, bottom - top) / 100f;
            }
        });
    }

    public Pen createPen() {
       return new Pen(this);
    }


    public void clear() {
        removeAllViews();
        bitmap.eraseColor(0);
        addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
}
