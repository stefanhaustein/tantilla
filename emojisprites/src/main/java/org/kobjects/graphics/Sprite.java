package org.kobjects.graphics;

import android.content.Context;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class Sprite extends PositionedViewHolder<ImageView> implements Animated {

    public static final String DEFAULT_FACE = "\ud83d\ude03";

    TextBox label;
    TextBox bubble;
    private float size;
    private String face;
    private float angle;

    boolean textDirty;

    public Sprite(Screen screen) {
        super(screen, new ImageView(screen.activity));
        Context context = screen.activity;

        view.wrapped.setAdjustViewBounds(true);
        view.wrapped.setScaleType(ImageView.ScaleType.FIT_CENTER);

        /*
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(Dimensions.dpToPx(context, 1));

        int basePx = Dimensions.dpToPx(context, 4);

        bubble.setPadding(2 * basePx ,  basePx,2*basePx , basePx);
        BubbleDrawable bubbleDrawable = new BubbleDrawable();
        bubbleDrawable.cornerBox = 3 * basePx;
        bubbleDrawable.backgroundPaint = backgroundPaint;
        bubbleDrawable.strokePaint = borderPaint;
        bubble.setBackground(bubbleDrawable);
        bubble.setTextColor(Color.BLACK);
        bubble.setVisibility(View.GONE);*/

        setFace(DEFAULT_FACE);

//        bubble.setClipToOutline(false);

    }
    public String getFace() {
        return face;
    }

    public float getSize() {
        return size;
    }

    public float getAngle() {
        return angle;
    }


    @Override
    public void syncUi() {
        if (textDirty) {
            textDirty = false;
            view.wrapped.setImageDrawable(Emojis.getDrawable(view.getContext(), face));
        }

        int intrinsicWidth = view.wrapped.getDrawable().getIntrinsicWidth();
        int intrinsicHeight = view.wrapped.getDrawable().getIntrinsicHeight();
        int intrinsicSize = Math.max(intrinsicWidth, intrinsicHeight);

        float imageScale = (screen.scale * size) / intrinsicSize;
        view.wrapped.setScaleX(imageScale);
        view.wrapped.setScaleY(imageScale);
        view.wrapped.setRotation(angle);

        float screenX = x * screen.scale + anchor.view.getWidth() / 2;

        view.setTranslationX(screenX - intrinsicWidth / 2);

        float scrY = anchor.view.getHeight() / 2 - y * screen.scale;

        view.setTranslationY(scrY - intrinsicHeight / 2);

        view.setTranslationZ(z);
    }


    public boolean setSize(float size) {
        if (size == this.size) {
            return false;
        }
        this.size = size;
        requestSync();
        return true;
    }

    public TextBox getLabel() {
        if (label == null) {
            label = new TextBox(screen);
            label.setAnchor(this);
            label.setTextColor(0xff000000);
            label.setFillColor(0xffffffff);
            label.setLineColor(0xff000000);
            label.setY((getHeightForAnchoring() + this.label.getHeightForAnchoring()) / -2);
        }
        return label;
    }

    public boolean setLabel(TextBox bubble) {
        if (label == this.label) {
            return false;
        }
        this.label = label;
        label.anchor = this;
        return true;
    }

    public TextBox getBubble() {
        if (bubble == null) {
            bubble = new TextBox(screen);
            bubble.setAnchor(this);
            bubble.setPadding(3);
            bubble.setTextColor(0xff000000);
            bubble.setFillColor(0xffffffff);
            bubble.setLineColor(0xff000000);
            bubble.setY(10 + (getHeightForAnchoring() + bubble.getHeightForAnchoring()) / 2);
            bubble.setCornerRadius(3);
        }
        return bubble;
    }

    public boolean setBubble(TextBox bubble) {
        if (bubble == this.bubble) {
            return false;
        }
        this.bubble = bubble;
        bubble.anchor = this;
        return true;
    }

    public boolean setFace(String face) {
        if (Objects.equals(face, this.face)) {
            return false;
        }
        this.face = face;
        textDirty = true;
        requestSync();
        return true;
    }

    public boolean setAngle(float angle) {
        if (angle == this.angle) {
            return false;
        }
        this.angle = angle;
        requestSync();
        return true;
    }


    @Override
    public float getWidthForAnchoring() {
        return size;
    }

    @Override
    public float getHeightForAnchoring() {
        return size;
    }

    public void say(String text) {
        getBubble().setText(text);
        getBubble().setVisible(!text.isEmpty());
    }

    @Override
    public void animate(float dt) {
        if (tag instanceof Animated) {
            ((Animated) tag).animate(dt);
        }
    }

    public Collection<Sprite> collisions() {
        if (anchor != screen) {
            return Collections.emptyList();
        }
        ArrayList<Sprite> result = new ArrayList<>();
        synchronized (screen.widgets) {
            for (PositionedViewHolder<?> widget : screen.widgets) {
                if (widget != this && widget instanceof Sprite) {
                    Sprite other = (Sprite) widget;
                    double distX = other.x - x;
                    double distY = other.y - y;
                    double minDist = (other.size + size) * 0.4;
                    if (distX * distX + distY * distY < minDist * minDist) {
                        result.add(other);
                    }
                }
            }
        }
        return result;
    }
}
