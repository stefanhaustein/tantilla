package org.kobjects.emojisprites;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import static android.graphics.PixelFormat.TRANSLUCENT;

class BubbleDrawable extends Drawable {

    float cornerBox;
    float arrowSize;
    Paint paint = new Paint();

    BubbleDrawable(float arrowSize, float cornerBox) {
        this.arrowSize = arrowSize;
        this.cornerBox = cornerBox;
        paint.setAntiAlias(true);
    }


    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(bounds);
    }

    @Override
    public void draw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        RectF bounds = new RectF(getBounds());
        paint.setColor(0xffffffff);
      //  canvas.drawRoundRect(bounds, 16, 16 , paint);

        Path path = new Path();
        RectF arcBox = new RectF();

        path.moveTo(bounds.left, bounds.top);
        arcBox.set(bounds.right - cornerBox, bounds.top, bounds.right, bounds.top + cornerBox);
        path.arcTo(arcBox, 270, 90, false);
        arcBox.set(bounds.right - cornerBox, bounds.bottom - cornerBox, bounds.right, bounds.bottom);
        path.arcTo(arcBox, 0, 90, false);
        arcBox.set(bounds.left + arrowSize, bounds.bottom - cornerBox, bounds.left + cornerBox + arrowSize, bounds.bottom);
        path.arcTo(arcBox, 90, 90, false);
        path.lineTo(bounds.left + arrowSize, bounds.top + arrowSize);
        path.close();

        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xffcccccc);
     //   canvas.drawRoundRect(bounds, 16, 16 , paint);
        canvas.drawPath(path, paint);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return TRANSLUCENT;
    }

}
