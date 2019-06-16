package org.kobjects.graphics;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class Screen extends ViewHolder<FrameLayout> {
  public final Activity activity;
  /**
   * Multiply with scale to get from virtual coordinates to px, divide to get from px to
   * virtual coordinates.
   */
  public float scale;
  ImageView imageView;
  Bitmap bitmap;
  float bitmapScale;
  public Dpad dpad;

  /**
   * Contains all positioned view holders including children.
   */
  Set<PositionedViewHolder<?>> widgets = Collections.newSetFromMap(new WeakHashMap<>());


  public Screen(@NonNull Activity activity) {
    super(new FrameLayout(activity));
    this.activity = activity;
    view.setClipChildren(false);

    int size = Dimensions.dpToPx(activity,200);

    bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    bitmapScale = size / 200f;

    imageView = new ImageView(activity);
    imageView.setImageBitmap(bitmap);
    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

    dpad = new Dpad(this);

    view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        scale = Math.min(right - left, bottom - top) / 200f;
        dpad.requestSync();
      }
    });

    view.setFocusableInTouchMode(true);

    view.addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    FrameLayout.LayoutParams dpadLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    dpadLayoutParams.gravity = Gravity.BOTTOM;
    view.addView(dpad.view, dpadLayoutParams);
  }

  public Pen createPen() {
    return new Pen(this);
  }


  public boolean dispatchKeyEvent(android.view.KeyEvent keyEvent) {
    System.out.println("KeyEvent: " + keyEvent);
    return false;
  }

  public void clearAll() {
    cls();
    synchronized (widgets) {
      for (PositionedViewHolder<?> widget : widgets) {
        widget.setVisible(false);
      }
    }
    widgets = Collections.newSetFromMap(new WeakHashMap<>());
  }

  public void cls() {
    bitmap.eraseColor(0);
    dpad.setVisible(false);
  }

  @Override
  public float getWidth() {
    return view.getWidth() / scale;
  }

  @Override
  public float getHeight() {
    return view.getHeight() / scale;
  }

  public void animate(float dt) {
    ArrayList<PositionedViewHolder<?>> copy = new ArrayList<>(widgets.size());
    synchronized (widgets) {
      copy.addAll(widgets);
    }
    for (PositionedViewHolder<?> widget : copy) {
      if (widget instanceof Sprite) {
        ((Sprite) widget).animate(dt);
      }
    }
  }
}
