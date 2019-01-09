package org.kobjects.graphics;

import android.view.View;
import android.view.ViewGroup;


class AnchorView<T extends View> extends ViewGroup {

    final T wrapped;

    public AnchorView(T wrapped) {
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
        int width = right - left;
        int height = bottom - top;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int childX = left - (width - childWidth) / 2;
            int childY = top - (height - childHeight) / 2;
            child.layout(childX, childY, childX + childWidth, childY + childHeight);
        }
    }
}
