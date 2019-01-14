package org.kobjects.graphics;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import java.util.Objects;

public class Sprite extends PositionedViewHolder<ImageView> {

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
    public String getLabel() {
        return label == null ? "" : label.getText();
    }

    public String getText() {
        return bubble == null ? "" : bubble.getText();
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
    public boolean setLabel(String label) {
        if (Objects.equals(label, getLabel())) {
            return false;
        }
        if (this.label == null) {
            this.label = new TextBox(screen);
            this.label.setAnchor(this);
            this.label.setTextColor(0xff000000);
            this.label.setFillColor(0xffffffff);
            this.label.setLineColor(0xff000000);
            this.label.setY((getHeightForAnchoring() + this.label.getHeightForAnchoring()) / -2);
        }
        this.label.setText(label);
        return true;
    }

    public boolean setText(String text) {
        if (Objects.equals(text, getText())) {
            return false;
        }
        if (this.bubble == null) {
            this.bubble = new TextBox(screen);
            this.bubble.setAnchor(this);
            this.bubble.setTextColor(0xff000000);
            this.bubble.setFillColor(0xffffffff);
            this.bubble.setLineColor(0xff000000);
            this.bubble.setY(5 + (getHeightForAnchoring() + bubble.getHeightForAnchoring()) / 2);
            this.bubble.setCornerRadius(5);
        }
        this.bubble.setText(text);
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
}
