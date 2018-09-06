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

    public Pen(Viewport viewport) {
        this.viewport = viewport;

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
        canvas.drawRect(x, y, x+ width, y + height, fillPaint);
        canvas.drawRect(x, y, x+ width, y + height, strokePaint);
    }

}
