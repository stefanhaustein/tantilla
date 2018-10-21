package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.widget.LinearLayout;

public class TitleView extends LinearLayout {

    AppCompatTextView textView;

    public TitleView(Context context, int backgroundColor) {
        super(context);
        setBackgroundColor(backgroundColor);

        textView = new AppCompatTextView(context);
        textView.setTextSize(20);
        textView.setTypeface(Typeface.MONOSPACE);

        int padding = Dimensions.dpToPx(context, 6);
        textView.setPadding(padding, padding, padding, padding);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, Dimensions.dpToPx(context, 48), 1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        addView(textView, layoutParams);
    }

    public void setTitle(String title) {
        textView.setText(title);
    }
}
