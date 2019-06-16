package org.kobjects.graphics;

import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class Sprite extends PositionedViewHolder<ImageView>  {

  public static final String DEFAULT_FACE = "\ud83d\ude03";

  TextBox label;
  TextBox bubble;
  private float size;
  private String face;
  private float angle;
  private float speed;
  private float direction;
  private float grow;
  private float fade;
  private float rotation;

  private EdgeMode edgeMode = EdgeMode.NONE;

  private boolean textDirty = true;
  private boolean sizeDirty = true;

  public Sprite(Screen screen) {
    super(screen, new android.support.v7.widget.AppCompatImageView(screen.activity));

   // view.wrapped.setAdjustViewBounds(true);
   view.wrapped.setScaleType(ImageView.ScaleType.FIT_XY);

    setFace(DEFAULT_FACE);
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
  boolean shouldBeAttached() {
    // Top level sprites without children will get checked for physical removal
    if (view.getChildCount() == 1 && anchor instanceof Screen) {
      return opacity > MIN_OPACITY
          && x - size / 2 < 200 && x + size / 2 > -200
          && y - size / 2 < 200 && y + size / 2 > -200;
    }
    return super.shouldBeAttached();
  }


  @Override
  public void syncUi() {
    if (textDirty) {
      textDirty = false;
      view.wrapped.setImageDrawable(Emojis.getDrawable(view.getContext(), face));
    }

    if (sizeDirty) {
      sizeDirty = false;
      // view.wrapped.setBackgroundColor((int) (Math.random() * 0xffffff) | 0xff000000);
      view.wrapped.setLayoutParams(new FrameLayout.LayoutParams(Math.round(screen.scale * size), Math.round(screen.scale * size)));
      view.wrapped.requestLayout();
      view.requestLayout();
    }
    view.wrapped.setRotation(angle);
  }


  public boolean setSize(float size) {
    if (size == this.size) {
      return false;
    }
    this.size = size;
    sizeDirty = true;
    requestSync();
    return true;
  }

  public TextBox getLabel() {
    if (label == null) {
      label = new TextBox(screen);
      label.setAnchor(this);
      label.setTextColor(0xff000000);
      label.setFillColor(0xffffffff);
      label.setLineColor(0xff000000);
      label.setY((getHeight() + this.label.getHeight()) / -2);
      label.setYAlign(YAlign.TOP);
    }
    return label;
  }

  public boolean setLabel(TextBox bubble) {
    if (bubble == this.label) {
      return false;
    }
    this.label = bubble;
    label.anchor = this;
    return true;
  }

  public TextBox getBubble() {
    if (bubble == null) {
      bubble = new TextBox(screen);
      bubble.setAnchor(this);
      bubble.setPadding(3);
      bubble.setTextColor(0xff000000);
      bubble.setFillColor(0xffffffff);
      bubble.setLineColor(0xff000000);
      bubble.setY(10);
      bubble.setYAlign(YAlign.BOTTOM);
      bubble.setCornerRadius(3);
    }
    return bubble;
  }

  public boolean setBubble(TextBox bubble) {
    if (bubble == this.bubble) {
      return false;
    }
    this.bubble = bubble;
    bubble.anchor = this;
    return true;
  }

  public boolean setEdgeMode(EdgeMode newValue) {
    if (edgeMode == newValue) {
      return false;
    }
    edgeMode = newValue;
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
  public float getWidth() {
    return size;
  }

  @Override
  public float getHeight() {
    return size;
  }

  public void say(String text) {
    getBubble().setText(text);
    getBubble().setVisible(!text.isEmpty());
  }

  public void animate(float dt) {
    boolean propertiesChanged = false;

    if (speed != 0.0) {
      propertiesChanged = true;
      double theta = Math.toRadians(90 - direction);
      double delta = dt * speed / 1000;
      double dx = Math.cos(theta) * delta;
      double dy = Math.sin(theta) * delta;
      x += dx;
      y += dy;

       if (edgeMode != EdgeMode.NONE && anchor == screen) {
        float radius = size / 2;
        switch (edgeMode) {
          case WRAP:
            if (dx > 0 && x - radius > screen.getWidth() / 2) {
              x = -screen.getWidth() / 2 - radius;
            } else if (dx < 0 && x + radius < screen.getWidth() / -2) {
              x = screen.getWidth() / 2 - radius;
            }
            if (dy > 0 && y - radius > screen.getHeight() / 2) {
              y = -screen.getHeight() / 2 - radius;
            } else if (dy < 0 && y + radius < screen.getHeight() / -2) {
              y = screen.getHeight() / 2 + radius;
            }
            break;
          case BOUNCE:
            if (dx > 0 && x + radius > screen.getWidth() / 2) {
              direction += dy < 0 ? 90 : -90;
            } else if (dx < 0 && x - radius < screen.getWidth() / -2) {
              direction += dy > 0 ? 90 : -90;
            }
            if (dy > 0 && y + radius > screen.getHeight() / 2) {
              direction += dx > 0 ? 90 : -90;
            } else if (dy < 0 && y - radius < screen.getHeight() / -2) {
              direction += dx < 0 ? 90 : -90;
            }
            break;
        }
      }
    }
    if (rotation != 0F) {
      propertiesChanged = true;
      angle += rotation * dt / 1000F;
    }
    if (grow != 0F) {
      propertiesChanged = true;
      size += grow * dt / 1000F;
    }
    if (fade != 0F) {
      propertiesChanged = true;
      opacity += fade * dt / 1000F;
    }

    if (propertiesChanged) {
      requestSync();
    }

    if (tag instanceof Animated) {
      ((Animated) tag).animate(dt, propertiesChanged);
    }
  }

  /**
   * Checks all sprites, as widgets is flattened.
   */
  public Collection<Sprite> collisions() {
    if (!shouldBeAttached()) {
      return Collections.emptyList();
    }
    float sx = getScreenCX();
    float sy = getScreenCY();
    synchronized (screen.widgets) {
      ArrayList<Sprite> result = new ArrayList<>();
      // StringBuilder debug = new StringBuilder();
      for (PositionedViewHolder<?> widget : screen.widgets) {
        if (widget != this && widget instanceof Sprite && widget.shouldBeAttached()) {
          Sprite other = (Sprite) widget;
          double distX = other.getScreenCX() - sx;
          double distY = other.getScreenCY() - sy;
          double minDist = (other.size + size) * 0.4;
          if (distX * distX + distY * distY < minDist * minDist) {
            result.add(other);
            // debug.append(other.face);
          }
        }
      }
      // say(debug.toString());
      return result;
    }
  }

  public float getSpeed() {
    return speed;
  }

  public boolean setSpeed(float speed) {
    if (speed != this.speed) {
      this.speed = speed;
      return true;
    }
    return false;
  }

  public float getDirection() {
    return direction;
  }

  public boolean setDirection(float direction) {
    if (this.direction != direction) {
      this.direction = direction;
      return true;
    }
    return false;
  }

  public EdgeMode getEdgeMode() {
    return edgeMode;
  }

  public float getGrow() {
    return grow;
  }

  public boolean setGrow(float grow) {
    if (this.grow != grow) {
      this.grow = grow;
      return true;
    }
    return false;
  }

  public float getFade() {
    return fade;
  }

  public boolean setFade(float fade) {
    if (this.fade != fade) {
      this.fade = fade;
      return true;
    }
    return false;
  }

  public float getRotation() {
    return rotation;
  }
  
  public boolean setRotation(float rotation) {
    if (this.rotation != rotation) {
      this.rotation = rotation;
      return true;
    }
    return false;
  }

  public float getDx() {
    return speed * (float) Math.cos(Math.toRadians(90 - direction));
  }

  public float getDy() {
    return speed *  (float) Math.sin(Math.toRadians(90 - direction));
  }

  boolean setDxy(float dx, float dy) {
    float newSpeed = (float) Math.sqrt(dx * dx + dy * dy);

    if (newSpeed == 0) {
      return setSpeed(0);
    }

    float newDirection = 90 - (float) Math.toDegrees(Math.atan2(dy, dx));
    return setSpeed(newSpeed) | setDirection(newDirection < 0 ? 360 + newDirection : newDirection);
  }

  public boolean setDx(float dx) {
    return setDxy(dx, getDy());
  }

  public boolean setDy(float dy) {
    return setDxy(getDx(), dy);
  }
}
