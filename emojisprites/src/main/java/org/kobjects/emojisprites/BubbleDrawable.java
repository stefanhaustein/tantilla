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
    float arrowDx;
    float arrowDy;
    Paint backgroundPaint;
    Paint strokePaint;

    BubbleDrawable(float cornerBox, float arrowDx, float arrowDy, Paint background, Paint stroke) {
        this.cornerBox = cornerBox;
        this.arrowDx = arrowDx;
        this.arrowDy = arrowDy;
        this.backgroundPaint = background;
        this.strokePaint = stroke;

    }


    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(bounds);
    }

    @Override
    public void draw(Canvas canvas) {

        RectF bounds = new RectF(getBounds());
        /*  canvas.drawRoundRect(bounds, 16, 16 , paint);
*/

        Path path = new Path();
        RectF arcBox = new RectF();

        // Top left
        path.moveTo(bounds.left + cornerBox, bounds.top);

        // top right corner
        arcBox.set(bounds.right - cornerBox, bounds.top, bounds.right, bounds.top + cornerBox);
        path.arcTo(arcBox, 270, 90, false);

        // bottom right corner
        arcBox.set(bounds.right - cornerBox, bounds.bottom - cornerBox, bounds.right, bounds.bottom);
        path.arcTo(arcBox, 0, 90, false);

        path.lineTo(bounds.centerX() + cornerBox/2, bounds.bottom);
        path.lineTo(bounds.centerX() + arrowDx, bounds.bottom + arrowDy);
        path.lineTo(bounds.centerX() - cornerBox/2, bounds.bottom);

        // bottom left corner
        arcBox.set(bounds.left, bounds.bottom - cornerBox, bounds.left + cornerBox, bounds.bottom);
        path.arcTo(arcBox, 90, 90, false);

        arcBox.set(bounds.left, bounds.top, bounds.left + cornerBox, bounds.top + cornerBox);
        path.arcTo(arcBox, 180, 90, false);

        path.close();

        canvas.drawPath(path, backgroundPaint);
        canvas.drawPath(path, strokePaint);
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
