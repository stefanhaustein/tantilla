package org.kobjects.asde.android.ide.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public abstract class ExpandableView<T extends View> extends LinearLayout {

    TitleView titleView;
    T contentView;
    int fullHeight;
    ValueAnimator heightAnimator;
    List<ExpandListener> expandListeners = new ArrayList<>();
    boolean expanded;

    public ExpandableView(Context context, T contentView) {
        super(context);
        this.contentView = contentView;
        setOrientation(VERTICAL);
        titleView = new TitleView(context);
        addView(titleView);
        addView(contentView);
    }

    public void setExpanded(final boolean expand, boolean animated) {
        if (expanded == expand) {
            return;
        }
        expanded = expand;

        if (expand) {
            syncContent();
            for (FunctionView.ExpandListener expandListener : expandListeners) {
                expandListener.notifyExpanding(this, animated);
            }
        } else {
            fullHeight = contentView.getMeasuredHeight();
        }

        if (heightAnimator != null) {
            heightAnimator.cancel();
            heightAnimator = null;
        }

        heightAnimator = ValueAnimator.ofInt(contentView.getHeight(), expanded ? fullHeight : 0);
        heightAnimator.setTarget(this);
        heightAnimator.setDuration(300);
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentView.getLayoutParams();
                layoutParams.height = (Integer) animation.getAnimatedValue();
                contentView.setLayoutParams(layoutParams);
            }
        });
        heightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                heightAnimator = null;
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentView.getLayoutParams();
                layoutParams.height = WRAP_CONTENT;
                contentView.setLayoutParams(layoutParams);
                if (!expanded) {
                    syncContent();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        heightAnimator.start();

        if (!animated) {
            heightAnimator.end();
        }
    }


    public void addExpandListener(ExpandListener expandListener) {
        expandListeners.add(expandListener);
    }


    public abstract void syncContent();



    public interface ExpandListener {
        void notifyExpanding(ExpandableView expandableView, boolean animated);
    }

}
