package org.kobjects.graphics;

import android.view.View;
import android.view.ViewGroup;


/**
 * Derives the size from the wrapped view. All views are positioned at the top left corner,
 * similar to FrameLayout.
 */
class AnchorLayout<T extends View> extends ViewGroup {

    final T wrapped;

    public AnchorLayout(T wrapped) {
        super(wrapped.getContext());
        this.wrapped = wrapped;
        if (wrapped != null) {
            addView(wrapped);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        }
        setMeasuredDimension(wrapped.getMeasuredWidth(), wrapped.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(left, top, left +  child.getMeasuredWidth(), top + child.getMeasuredHeight());
        }
    }
}
