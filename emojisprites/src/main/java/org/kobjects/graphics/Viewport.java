package org.kobjects.graphics;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;

public class Viewport extends FrameLayout {
    public final Activity activity;
    public float scale;

    public Viewport(@NonNull Activity activity) {
        super(activity);
        this.activity = activity;
        setClipChildren(false);

        addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                scale = Math.min(right - left, bottom - top) / 100f;
            }
        });
    }




    public void clear() {
        removeAllViews();
    }
}
