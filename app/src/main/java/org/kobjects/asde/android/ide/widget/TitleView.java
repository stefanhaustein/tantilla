package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.Gravity;
import android.widget.LinearLayout;

import org.kobjects.asde.android.ide.Dimensions;
import org.kobjects.asde.R;

public class TitleView extends LinearLayout {

    AppCompatTextView textView;
    public IconButton moreButton;

    public TitleView(Context context, int backgroundColor, OnClickListener contextMenuClickListener) {
        super(context);
        setBackgroundColor(backgroundColor);

        textView = new AppCompatTextView(context);
        textView.setTextSize(20);
        textView.setTypeface(Typeface.MONOSPACE);
        textView.setMaxLines(1);

        int padding = Dimensions.dpToPx(context, 6);
        textView.setPadding(padding, padding, padding, padding);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, Dimensions.dpToPx(context, 48), 1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        addView(textView, layoutParams);

        if (contextMenuClickListener != null) {
            moreButton = new IconButton(getContext(), R.drawable.baseline_more_vert_24);
            addView(moreButton);
            moreButton.setOnClickListener(contextMenuClickListener);
        }
    }

    public void setTitle(String title) {
        textView.setText(title);
    }
}
