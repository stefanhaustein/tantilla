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

    boolean right;
    float cornerBox;
    float arrowSize;
    Paint paint = new Paint();

    BubbleDrawable(float arrowSize, float cornerBox, boolean right) {
        this.arrowSize = arrowSize;
        this.cornerBox = cornerBox;
        this.right = right;
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
        if (right) {
            paint.setColor(0xffC5CAE9);
        } else {
            paint.setColor(0xffffffff);
        }
      //  canvas.drawRoundRect(bounds, 16, 16 , paint);

        Path path = new Path();
        RectF arcBox = new RectF();

        if (right) {
            path.moveTo(bounds.right, bounds.top);
            arcBox.set(bounds.left, bounds.top, bounds.left + cornerBox, bounds.top + cornerBox);
            path.arcTo(arcBox, 270, -90, false);
            arcBox.set(bounds.left, bounds.bottom - cornerBox, bounds.left + cornerBox, bounds.bottom);
            path.arcTo(arcBox, 180, -90, false);
            arcBox.set(bounds.right - cornerBox - arrowSize, bounds.bottom - cornerBox, bounds.right - arrowSize, bounds.bottom);
            path.arcTo(arcBox, 90, -90, false);
            path.lineTo(bounds.right - arrowSize, bounds.top + arrowSize);
        } else {
            path.moveTo(bounds.left, bounds.top);
            arcBox.set(bounds.right - cornerBox, bounds.top, bounds.right, bounds.top + cornerBox);
            path.arcTo(arcBox, 270, 90, false);
            arcBox.set(bounds.right - cornerBox, bounds.bottom - cornerBox, bounds.right, bounds.bottom);
            path.arcTo(arcBox, 0, 90, false);
            arcBox.set(bounds.left + arrowSize, bounds.bottom - cornerBox, bounds.left + cornerBox + arrowSize, bounds.bottom);
            path.arcTo(arcBox, 90, 90, false);
            path.lineTo(bounds.left + arrowSize, bounds.top + arrowSize);
        }
        path.close();

    /*    Path path = new Path();
        path.moveTo(bounds.right, bounds.top + 20);
        path.lineTo(bounds.right + 20, bounds.top);
        path.lineTo(bounds.right - 20, bounds.top);*/
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
