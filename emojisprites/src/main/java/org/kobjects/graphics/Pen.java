package org.kobjects.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class Pen {
    Viewport viewport;
//    Bitmap bitmap;
    Canvas canvas;
    Paint strokePaint = new Paint();
    Paint fillPaint = new Paint();

    float sx(float x) {
        return (x + 50) * viewport.bitmapScale;
    }

    float sy(float y) {
        return (50 - y) * viewport.bitmapScale;
    }

    public Pen(Viewport viewport) {
        this.viewport = viewport;

        strokePaint.setAntiAlias(true);
        fillPaint.setAntiAlias(true);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.BLUE);
    }

    void validate() {
       if (canvas == null) {
           canvas = new Canvas(viewport.bitmap);
       }
    }

    public void drawLine(float x0, float y0, float x1, float y1) {
        validate();
        canvas.drawLine(x0, y0, x1, y1, strokePaint);
    }

    public void drawRect(float  x, float y, float width, float height) {
        validate();
        canvas.drawRect(sx(x), sy(y), sx(x+ width), sy(y + height), fillPaint);
        canvas.drawRect(sx(x), sy(y), sx(x+ width), sy(y + height), strokePaint);
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

}
