package org.kobjects.graphics;

import android.view.View;

abstract class ViewHolder<T extends View> implements Runnable {
    final Viewport viewport;
    final AnchorView<T> view;
    boolean syncRequested;

    ViewHolder(Viewport viewport, T view) {
        this.viewport = viewport;
        this.view = new AnchorView<>(view);
    }


    void requestSync() {
        if (!syncRequested) {
            syncRequested = true;
            viewport.activity.runOnUiThread(this);
        }
    }

    public AnchorView<T> getView() {
        return view;
    }
}
