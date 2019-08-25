package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ResizableFrameLayout extends FrameLayout{
    static final int PADDING = 12;
    static final int RESIZE_AREA = 24;

    public ResizableFrameLayout(@NonNull Context context) {
        super(context);
        setBackgroundColor(0x088888888);
        int padding = Dimensions.dpToPx(context, PADDING);
        setPadding(padding, padding, padding, padding);


        setOnTouchListener(new OnTouchListener() {
            float rawX0;
            float rawY0;
            float sizeXFactor;
            float sizeYFactor;
            float tX0;
            float tY0;
            int height0;
            int width0;
            boolean translateX;
            boolean translateY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        width0 = getWidth();
                        height0 = getHeight();

                        if (event.getX() < Dimensions.dpToPx(getContext(),RESIZE_AREA)) {
                            sizeXFactor = -1;
                            translateX = false;
                        } else if (event.getX() > width0 - Dimensions.dpToPx(getContext(), RESIZE_AREA)) {
                            sizeXFactor = 1;
                            translateX = true;
                        } else {
                            sizeXFactor = 0;
                            translateX = true;
                        }

                        if (event.getY() < Dimensions.dpToPx(getContext(),RESIZE_AREA)) {
                            sizeYFactor = -1;
                            translateY = true;
                        } else if (event.getY() > height0 - Dimensions.dpToPx(getContext(), RESIZE_AREA)) {
                            sizeYFactor = 1;
                            translateY = false;
                        } else {
                            sizeYFactor = 0;
                            translateY = true;
                        }

                        // Move: both or none.
                        if (sizeXFactor == 0 && sizeYFactor != 0) {
                            translateX = false;
                        } else if (sizeXFactor != 0 && sizeYFactor == 0) {
                            translateY = false;
                        }

                        tX0 = getTranslationX();
                        tY0 = getTranslationY();
                        rawX0 = event.getRawX();
                        rawY0 = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        event.getRawX();
                        event.getRawY();
                        float dx = event.getRawX() - rawX0;
                        float dy = event.getRawY() - rawY0;

                        ViewGroup.LayoutParams layoutParams = getLayoutParams();
                        layoutParams.width = Math.max(Math.round(width0 + sizeXFactor * dx), 3 *Dimensions.dpToPx(getContext(),RESIZE_AREA));
                        layoutParams.height = Math.max(Math.round(height0 + sizeYFactor * dy), 3 *Dimensions.dpToPx(getContext(),RESIZE_AREA));
                        setLayoutParams(layoutParams);

                        if (translateX) {
                            setTranslationX(tX0 + dx);
                        }
                        if (translateY) {
                            setTranslationY(tY0 + dy);
                        }
                        break;

                }
                return true;
            }
        });
    }



}
