package org.kobjects.graphics;

import android.view.View;
import android.view.ViewGroup;


class AnchorView<T extends View> extends ViewGroup {

    final T wrapped;

    public AnchorView(T wrapped) {
        super(wrapped.getContext());
        this.wrapped = wrapped;
        addView(wrapped);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);

        }
        setMeasuredDimension(wrapped.getMeasuredWidth(), wrapped.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
        }
    }
}
