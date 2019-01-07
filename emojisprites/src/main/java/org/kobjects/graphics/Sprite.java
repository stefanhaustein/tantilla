package org.kobjects.graphics;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

public class Sprite extends PositionedViewHolder<ImageView> {

    public static final String DEFAULT_FACE = "\ud83d\ude03";

    Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    TextView labelView;
    TextView bubble;
    private float size;
    private String label;
    private String text;
    private String face;
    private float angle;

    boolean textDirty;

    public Sprite(Screen screen) {
        super(screen, new ImageView(screen.activity));
        Context context = screen.activity;

        view.wrapped.setAdjustViewBounds(true);
        view.wrapped.setScaleType(ImageView.ScaleType.FIT_CENTER);

        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(Dimensions.dpToPx(context, 1));

        labelView = new TextView(context);
        //  labelView.setBackgroundColor(Color.WHITE);
        labelView.setTextColor(Color.BLACK);
        labelView.setVisibility(View.GONE);

        bubble = new TextView(context);

        int basePx = Dimensions.dpToPx(context, 4);

        bubble.setPadding(2 * basePx ,  basePx,2*basePx , basePx);
        bubble.setBackground(new BubbleDrawable(3 * basePx, 0, 3 * basePx, backgroundPaint, borderPaint));
        bubble.setTextColor(Color.BLACK);
        bubble.setVisibility(View.GONE);

        setFace(DEFAULT_FACE);

//        bubble.setClipToOutline(false);

    }
    public String getLabel() {
        return label;
    }

    public String getText() {
        return text;
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

        if (labelView.getParent() == null) {
           // screen.view.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            screen.view.addView(labelView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            screen.view.addView(bubble, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        if (textDirty) {
            textDirty = false;

            if (label == null || label.isEmpty()) {
                labelView.setText("");
                labelView.setVisibility(View.GONE);
            } else {
                labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 5 * screen.scale);
                labelView.setVisibility(View.VISIBLE);
                labelView.setText(" " + label + " ");
                setX(x);
            }

            if (text == null || text.isEmpty()) {
                bubble.setText("");
                bubble.setVisibility(View.GONE);
            } else {
                bubble.setVisibility(View.VISIBLE);
                bubble.setText(text);
                bubble.measure(View.MeasureSpec.AT_MOST | Dimensions.dpToPx(bubble.getContext(), 160), View.MeasureSpec.UNSPECIFIED);
                bubble.getLayoutParams().width = bubble.getMeasuredWidth();
            }
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
        labelView.setTranslationX(screenX - labelView.getPaint().measureText(labelView.getText().toString()) / 2);
        bubble.setTranslationX(screenX - bubble.getMeasuredWidth() / 2);

        float heightPx = intrinsicHeight * view.getScaleY();
        float scrY = anchor.view.getHeight() / 2 - y * screen.scale;

        view.setTranslationY(scrY - intrinsicHeight / 2);

        labelView.setTranslationY(scrY + heightPx / 2);
        bubble.setTranslationY(scrY - bubble.getMeasuredHeight() - 20 - heightPx / 2);
        view.setTranslationZ(z);
        labelView.setTranslationZ(z);
        bubble.setTranslationZ(z);
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
        if (Objects.equals(label, this.label)) {
            return false;
        }
        this.label = label;
        textDirty = true;
        requestSync();
        return true;
    }

    public boolean setText(String text) {
        if (Objects.equals(text, this.text)) {
            return false;
        }
        this.text = text;
        textDirty = true;
        requestSync();
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


}
