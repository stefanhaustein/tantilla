package org.kobjects.graphics;

import android.view.View;

public abstract class AbstractViewWrapper<T extends View> implements Runnable {
    T view;
    boolean syncRequested;
    Viewport viewport;
    float x;
    float y;

    AbstractViewWrapper(Viewport viewport, T view) {
        this.viewport = viewport;
        this.view = view;
        view.setTag(this);

        viewport.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (AbstractViewWrapper.this.view.getParent() != null) {
                    requestSync();
                }
            }
        });
    }


    public void setX(float x) {
        if (x != this.x) {
            this.x = x;
            requestSync();
        }
    }

    public void setY(float y) {
        if (y != this.y) {
            this.y = y;
            requestSync();
        }
    }

    void requestSync() {
        if (!syncRequested) {
            syncRequested = true;
            viewport.activity.runOnUiThread(this);
        }
    }
}
