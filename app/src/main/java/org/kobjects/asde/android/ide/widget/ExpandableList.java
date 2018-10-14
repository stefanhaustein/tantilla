package org.kobjects.asde.android.ide.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ExpandableList extends LinearLayout {
    ValueAnimator animator;

    int currentHeight;
    int targetHeight;
    boolean removeAllPending;

    public ExpandableList(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }


    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        if (animator != null) {
            setMeasuredDimension(getMeasuredWidth(), currentHeight);
        } else {
            super.onMeasure(widthSpec, heightSpec);
            targetHeight = getMeasuredHeight();
            if (targetHeight != currentHeight) {
                startAnimation();
            }
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams layoutParams) {
        if (removeAllPending) {
            if (animator != null) {
                animator.cancel();
            }
            removeAllPending = false;
            removeAllViews();
        }
        super.addView(child, index, layoutParams);
    }

    @Override
    public int getChildCount() {
        return removeAllPending ? 0 : super.getChildCount();
    }

    @Override
    public void removeAllViews() {
        if (getChildCount() != 0) {
            targetHeight = 0;
            removeAllPending = true;
            startAnimation();
        }
    }


    void startAnimation() {
        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofInt(currentHeight, targetHeight);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentHeight = (Integer) animation.getAnimatedValue();
                requestLayout();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animator = null;
                if (removeAllPending) {
                    ExpandableList.super.removeAllViews();
                    removeAllPending = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animator = null;
                if (removeAllPending) {
                    ExpandableList.super.removeAllViews();
                    removeAllPending = false;
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

}
