package org.kobjects.graphics;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * Derives the size from the wrapped view. All views are positioned at the top left corner,
 * similar to FrameLayout.
 */
class AnchorLayout<T extends View> extends FrameLayout {

  final T wrapped;

  public AnchorLayout(T wrapped) {
    super(wrapped.getContext());
    this.wrapped = wrapped;
    if (wrapped != null) {
      addView(wrapped);
    }
    setClipChildren(false);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
   super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(wrapped.getMeasuredWidth(), wrapped.getMeasuredHeight());
  }
}
