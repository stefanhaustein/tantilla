package org.kobjects.emojisprites;

import android.graphics.Color;
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
    ViewGroup container;
    ImageView imageView;
    TextView labelView;
    TextView speechBubble;
    float width;
    float height;

    public EmojiSprite(FrameLayout container) {
        this.container = container;
        imageView = new ImageView(container.getContext());
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        List<EmojiRange> emojis = EmojiUtils.emojis("\ud83d\ude03");
        if (emojis.size() > 0) {
            Emoji emoji = emojis.get(0).emoji;
            imageView.setImageDrawable(emoji.getDrawable(container.getContext()));
        }

        labelView = new TextView(container.getContext());
        labelView.setTranslationY(imageView.getDrawable().getIntrinsicHeight());
        labelView.setBackgroundColor(Color.WHITE);
    }

    public boolean isVisible() {
        return imageView.getParent() != null;
    }

    public void show() {
        if (!isVisible()) {
            container.addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            container.addView(labelView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
    }

    public void setY(float v) {
        imageView.setTranslationY(v);
        labelView.setTranslationY(v + height * 1.1f);
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

    public ImageView getImageView() {
        return imageView;
    }
}
