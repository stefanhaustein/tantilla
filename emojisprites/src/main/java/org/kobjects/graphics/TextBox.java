package org.kobjects.graphics;

import android.graphics.Color;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;


public class TextBox extends PositionedViewHolder<TextView> {

  private boolean layoutDirty = true;
  private String text = "";
  private float size = 10;
  private int textColor = Color.GRAY;
  private int fillColor;
  private int lineColor;
  private float lineWidth = 0.5f;
  private BubbleDrawable bubbleDrawable;
  private float padding = 5;
  private float cornerRadius = 0;
  private TextPaint textPaint;

  public TextBox(Screen screen) {
    super(screen, new TextView(screen.activity));
    this.textPaint = view.wrapped.getPaint();
    view.wrapped.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        requestSync();
      }
    });
  }

  @Override
  public void syncUi() {
    if (layoutDirty) {
      layoutDirty = false;
      view.wrapped.setText(text);
      int sp = Math.round(padding * screen.scale);
      view.wrapped.setPadding(sp, sp, sp, sp);
      view.wrapped.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * screen.scale);
      view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
      view.wrapped.requestLayout();
      view.requestLayout();
    }

    view.wrapped.setTextColor(textColor);

    if (cornerRadius == 0 && (lineColor == 0 || lineWidth == 0)) {
      if (bubbleDrawable != null) {
        view.wrapped.setBackground(null);
        //     view.wrapped.setClipToOutline(true);
        bubbleDrawable = null;
      }
      view.wrapped.setBackgroundColor(fillColor);
    } else {
      if (bubbleDrawable == null) {
        bubbleDrawable = new BubbleDrawable();
        view.wrapped.setBackground(bubbleDrawable);
        view.wrapped.setClipToOutline(false);
        //      view.setClipChildren(false);
      }
      bubbleDrawable.cornerBox = cornerRadius * 2 * screen.scale;
      bubbleDrawable.strokePaint.setColor(lineColor);
      bubbleDrawable.strokePaint.setStrokeWidth(lineWidth * screen.scale);
      bubbleDrawable.backgroundPaint.setColor(fillColor);

      if (yAlign == YAlign.BOTTOM && anchor != screen && getY() > 0) {
        bubbleDrawable.arrowDy = screen.scale * y;
        bubbleDrawable.arrowDx = screen.scale * -x / 2;
      } else {
        bubbleDrawable.arrowDy = 0;
        bubbleDrawable.arrowDx = 0;
      }
      bubbleDrawable.invalidateSelf();
//            view.wrapped.invalidate();
    }
  }

  public boolean setSize(float size) {
    if (size == this.size) {
      return false;
    }
    this.size = size;
    layoutDirty = true;
    requestSync();
    return true;
  }

  public boolean setText(String text) {
    if (Objects.equals(text, this.text)) {
      return false;
    }
    this.text = text;
    layoutDirty = true;
    requestSync();
    return true;
  }

  public boolean setFillColor(int fillColor) {
    if (fillColor == this.fillColor) {
      return false;
    }
    this.fillColor = fillColor;
    requestSync();
    return true;
  }

  public boolean setLineColor(int lineColor) {
    if (lineColor == this.lineColor) {
      return false;
    }
    this.lineColor = lineColor;
    requestSync();
    return true;
  }

  public boolean setTextColor(int textColor) {
    if (textColor == this.textColor) {
      return false;
    }
    this.textColor = textColor;
    requestSync();
    return true;
  }

  public boolean setLineWidth(float lineWidth) {
    if (lineWidth == this.lineWidth) {
      return false;
    }
    this.lineWidth = lineWidth;
    requestSync();
    return true;
  }


  public boolean setCornerRadius(float cornerRadius) {
    if (cornerRadius == this.cornerRadius) {
      return false;
    }
    this.cornerRadius = cornerRadius;
    requestSync();
    return true;
  }



  public boolean setPadding(float padding) {
    if (padding == this.padding) {
      return false;
    }
    this.padding = padding;
    requestSync();
    return true;
  }

  public float getSize() {
    return size;
  }

  public String getText() {
    return text;
  }

  @Override
  public float getWidth() {
    return /*layoutDirty||true ? calculateSize()[0] / screen.scale + 2 * padding :*/ view.wrapped.getMeasuredWidth() / screen.scale;
  }

  @Override
  public float getHeight() {
    return /*layoutDirty||true ? calculateSize()[1] / screen.scale + 2 * padding :*/ view.wrapped.getMeasuredHeight() / screen.scale;
  }

  float[] calculateSize() {
    textPaint.setTextSize(size * screen.scale);
    StaticLayout staticLayout = new StaticLayout(text, textPaint, Integer.MAX_VALUE, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
    return new float[] {staticLayout.getWidth(), Math.min(staticLayout.getHeight(), size * screen.scale)};
  }

  public int getLineColor() {
    return lineColor;
  }

  public float getLineWidth() {
    return lineWidth;
  }

  public int getFillColor() {
    return fillColor;
  }

  public float getCornerRadius() {
    return cornerRadius;
  }

  public int getTextColor() {
    return textColor;
  }

  public float getPadding() {
    return padding;
  }
}
