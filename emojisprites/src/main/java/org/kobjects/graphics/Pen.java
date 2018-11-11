package org.kobjects.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class Pen {
    Viewport viewport;
//    Bitmap bitmap;
    Canvas canvas;
    Paint strokePaint = new Paint();
    Paint fillPaint = new Paint();
    Paint clearPaint;

    float sx(float x) {
        return (x + 100) * viewport.bitmapScale;
    }

    float sy(float y) {
        return (100 - y) * viewport.bitmapScale;
    }

    public Pen(Viewport viewport) {
        this.viewport = viewport;

        strokePaint.setAntiAlias(true);
        fillPaint.setAntiAlias(true);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.BLUE);

        setTextSize(10);
    }

    void validate() {
       if (canvas == null) {
           canvas = new Canvas(viewport.bitmap);
       }
       viewport.postInvalidate();
    }

    public void drawLine(float x0, float y0, float x1, float y1) {
        validate();
        canvas.drawLine(sx(x0), sy(y0), sx(x1), sy(y1), strokePaint);
    }

    public void drawRect(float  x, float y, float width, float height) {
        validate();
        canvas.drawRect(sx(x), sy(y), sx(x+ width), sy(y + height), fillPaint);
        canvas.drawRect(sx(x), sy(y), sx(x+ width), sy(y + height), strokePaint);
    }

    public void drawText(float x, float y, String text) {
        validate();
        float sx = sx(x);
        float sy = sy(y);
        if ((fillPaint.getColor() & 0xff000000) != 0) {
            canvas.drawText(text, sx, sy, fillPaint);
        }
        if ((strokePaint.getColor() & 0xff000000) != 0) {
            canvas.drawText(text, sx, sy, strokePaint);
        }
    }


    public void clearRect(float  x, float y, float width, float height) {
        validate();
        if (clearPaint == null) {
            clearPaint = new Paint();
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            clearPaint.setColor(Color.TRANSPARENT);
            clearPaint.setStyle(Paint.Style.FILL);
        }
        canvas.drawRect(sx(x), sy(y), sx(x+ width), sy(y + height), clearPaint);
    }

    public boolean setTextSize(float size) {
        if (strokePaint.getTextSize() == size * viewport.bitmapScale) {
            return false;
        }
        strokePaint.setTextSize(size * viewport.bitmapScale);
        fillPaint.setTextSize(size * viewport.bitmapScale);
        return true;
    }

    public boolean setFillColor(int argb) {
        if (argb == fillPaint.getColor()) {
            return false;
        }
        fillPaint.setColor(argb);
        return true;
    }

    public int getFillColor() {
        return fillPaint.getColor();
    }

    public boolean setStrokeColor(int argb) {
        if (argb == strokePaint.getColor()) {
            return false;
        }
        strokePaint.setColor(argb);
        return true;
    }

    public int getStrokeColor() {
        return strokePaint.getColor();
    }

    public float getTextSize() {
        return strokePaint.getTextSize() / viewport.bitmapScale;
    }
}
