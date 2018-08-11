package org.kobjects.emojisprites;

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
    }

    public boolean isVisible() {
        return imageView.getParent() != null;
    }

    public void show() {
        if (!isVisible()) {
            container.addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    public void setSize(float width, float height) {
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.height = Math.round(height);
        layoutParams.width = Math.round(width);
        imageView.setLayoutParams(layoutParams);
    }

    public int getIntrinsicWidth() {
        return imageView.getDrawable().getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        return imageView.getDrawable().getIntrinsicWidth();
    }

    public void setX(float v) {
        imageView.setTranslationX(v);
    }

    public void setY(float v) {
        imageView.setTranslationY(v);
    }

    public ImageView getImageView() {
        return imageView;
    }
}
