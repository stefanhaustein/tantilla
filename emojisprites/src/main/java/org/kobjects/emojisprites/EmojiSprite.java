package org.kobjects.emojisprites;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiRange;
import com.vanniktech.emoji.EmojiUtils;
import com.vanniktech.emoji.emoji.Emoji;

import java.util.List;

public class EmojiSprite {
    Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    ViewGroup container;
    ImageView imageView;
    TextView labelView;
    TextView bubble;
    float width;
    float height;

    public EmojiSprite(FrameLayout container) {
        this.container = container;
        imageView = new ImageView(container.getContext());
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(Dimensions.dpToPx(container.getContext(), 1));

        setFace("\ud83d\ude03");


        labelView = new TextView(container.getContext());
        labelView.setTranslationY(imageView.getDrawable().getIntrinsicHeight());
        labelView.setBackgroundColor(Color.WHITE);
        labelView.setTextColor(Color.BLACK);

        bubble = new TextView(container.getContext());

        int basePx = Dimensions.dpToPx(container.getContext(), 4);

        bubble.setPadding(2 * basePx ,  basePx,2*basePx , basePx);
        bubble.setBackground(new BubbleDrawable(3 * basePx, 0, 3 * basePx, backgroundPaint, borderPaint));
        bubble.setTextColor(Color.BLACK);

//        bubble.setClipToOutline(false);

    }

    public boolean isVisible() {
        return imageView.getParent() != null;
    }

    public void show() {
        if (!isVisible()) {
            container.addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            container.addView(labelView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            container.addView(bubble, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    public void setSize(float width, float height) {
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.height = Math.round(height);
        layoutParams.width = Math.round(width);
        imageView.setLayoutParams(layoutParams);
        this.width = width;
        this.height = height;
        setX(imageView.getTranslationX());
        setY(imageView.getTranslationY());
    }

    public int getIntrinsicWidth() {
        return imageView.getDrawable().getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        return imageView.getDrawable().getIntrinsicWidth();
    }

    public void setX(float v) {
        imageView.setTranslationX(v);
        labelView.setTranslationX(v + width / 2 - labelView.getPaint().measureText(labelView.getText().toString()) / 2);
        bubble.setTranslationX(v + width / 2 - bubble.getMeasuredWidth() / 2);
    }

    public void setY(float v) {
        imageView.setTranslationY(v);
        labelView.setTranslationY(v + height * 1.1f);
        bubble.setTranslationY(v - bubble.getMeasuredHeight() - 20);
    }

    public void setLabel(String label) {
        if (label == null || label.isEmpty()) {
            labelView.setText("");
            labelView.setVisibility(View.GONE);
        } else {
            labelView.setVisibility(View.VISIBLE);
            labelView.setText(" " + label + " ");
            setX(imageView.getTranslationX());
        }
    }

    public void setText(String text) {
        if (text == null || text.isEmpty()) {
            bubble.setText("");
            bubble.setVisibility(View.GONE);
        } else {
            bubble.setVisibility(View.VISIBLE);
            bubble.setText(text);
            bubble.measure(View.MeasureSpec.AT_MOST | Dimensions.dpToPx(bubble.getContext(), 160), View.MeasureSpec.UNSPECIFIED);
            bubble.getLayoutParams().width = bubble.getMeasuredWidth();

            setX(imageView.getTranslationX());
            setY(imageView.getTranslationY());
        }
    }


    public ImageView getImageView() {
        return imageView;
    }

    public void setFace(String s) {
        List<EmojiRange> emojis = EmojiUtils.emojis(s);
        if (emojis.size() > 0) {
            Emoji emoji = emojis.get(0).emoji;
            imageView.setImageDrawable(emoji.getDrawable(container.getContext()));
        }

    }
}
