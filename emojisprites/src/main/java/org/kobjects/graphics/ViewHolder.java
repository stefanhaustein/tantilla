package org.kobjects.graphics;

import android.view.View;

abstract class ViewHolder<T extends View> implements Runnable {
    final Viewport viewport;
    final T view;
    boolean syncRequested;

    ViewHolder(Viewport viewport, T view) {
        this.viewport = viewport;
        this.view = view;
    }


    void requestSync() {
        if (!syncRequested) {
            syncRequested = true;
            viewport.activity.runOnUiThread(this);
        }
    }

    public T getView() {
        return view;
    }
}
