package org.kobjects.asde.library.ui;

import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Array;
import org.kobjects.asde.lang.type.ArrayType;
import org.kobjects.asde.lang.type.Method;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.graphics.Animated;
import org.kobjects.typesystem.*;
import org.kobjects.graphics.Sprite;

import java.util.Collection;
import java.util.List;

public class SpriteAdapter extends Instance implements Animated {
  public static final Classifier CLASSIFIER =
      new Classifier(SpriteAdapter.SpriteMetaProperty.values()) {
        @Override
        public SpriteAdapter createInstance() {
                    throw new RuntimeException("Use screen.createSprite()");
                }
  };
  public static EnumType EDGE_MODE = new EnumType(EdgeMode.values());

  final Sprite sprite;

  final NumberProperty x = new NumberProperty(SpriteMetaProperty.x);
  final NumberProperty y = new NumberProperty(SpriteMetaProperty.y);
  final NumberProperty z = new NumberProperty(SpriteMetaProperty.z);
  final NumberProperty dx = new NumberProperty(SpriteMetaProperty.dx);
  final NumberProperty dy = new NumberProperty(SpriteMetaProperty.dy);
  final NumberProperty size = new NumberProperty(SpriteMetaProperty.size);
  final NumberProperty angle = new NumberProperty(SpriteMetaProperty.angle);
  final NumberProperty left = new NumberProperty(SpriteMetaProperty.left);
  final NumberProperty right = new NumberProperty(SpriteMetaProperty.right);
  final NumberProperty top = new NumberProperty(SpriteMetaProperty.top);
  final NumberProperty bottom = new NumberProperty(SpriteMetaProperty.bottom);
  final ObjectProperty bubble = new ObjectProperty(SpriteMetaProperty.bubble);
  final ObjectProperty label = new ObjectProperty(SpriteMetaProperty.label);
  final ObjectProperty face = new ObjectProperty(SpriteMetaProperty.face);
  final ObjectProperty anchor = new ObjectProperty(SpriteMetaProperty.anchor);
  final PhysicalProperty<EdgeMode> edgeMode = new PhysicalProperty<>(EdgeMode.NONE);
  final PhysicalProperty<Double> speed = new PhysicalProperty<>(0.0);
  final PhysicalProperty<Double> direction = new PhysicalProperty<>(0.0);
  final PhysicalProperty<Double> rotation = new PhysicalProperty<>(0.0);
  final LazyProperty<Array> collisions = new LazyProperty<Array>() {
        @Override
        protected Array compute() {
      Collection<Sprite> collisions = sprite.collisions();
      Object[] adapters = new Object[collisions.size()];
      int index = 0;
      for (Sprite sprite : collisions) {
        adapters[index++] = sprite.getTag();
      }
      return new Array(SpriteAdapter.CLASSIFIER, adapters); }
  };

  public SpriteAdapter(final ScreenAdapter screen) {
        super(CLASSIFIER);
        sprite = new Sprite(screen.getScreen());
        sprite.setTag(this);
        sprite.setSize(10);

        speed.addListener(p -> {dx.notifyChanged(); dy.notifyChanged();});
        direction.addListener(p -> {dx.notifyChanged(); dy.notifyChanged();});
   }

  @Override
  public Property getProperty(PropertyDescriptor property) {
    switch ((SpriteMetaProperty) property) {
      case x: return x;
      case y: return y;
      case z: return z;
      case dx: return dx;
      case dy: return dy;
      case left: return left;
      case right: return right;
      case top: return top;
      case bottom: return bottom;
      case size: return size;
      case angle: return angle;
      case label: return label;
      case bubble: return bubble;
      case face: return face;
      case anchor: return anchor;
      case speed: return speed;
      case direction: return direction;
      case rotation: return rotation;
      case collisions: return collisions;
      case edgeMode: return edgeMode;
      case say: return new Method((FunctionType) SpriteMetaProperty.say.type()) {
        @Override
        public Object call(EvaluationContext evaluationContext, int paramCount) {
          sprite.say((String) (evaluationContext.getParameter(0)));
          return null;
        }};
    }
    throw new IllegalArgumentException();
  }

    @Override
    public void animate(float dt) {
        double s = speed.get();
        if (s != 0.0) {
            double theta = Math.toRadians(90 - direction.get());
            double delta = dt * s / 1000;
            double dx = Math.cos(theta) * delta;
            double dy = Math.sin(theta) * delta;
            x.set(x.get() + dx);
            y.set(y.get() + dy);

            EdgeMode edgeMode = this.edgeMode.get();
            if (edgeMode != EdgeMode.NONE && sprite.getAnchor() == sprite.getScreen()) {
              float radius = sprite.getSize() / 2;
              switch (edgeMode) {
                case WRAP:
                  if (dx > 0 && sprite.getX() - radius > sprite.getScreen().getWidth() / 2) {
                    sprite.setX(-sprite.getScreen().getWidth() / 2 - radius);
                  } else if (dx < 0 && sprite.getX() + radius < sprite.getScreen().getWidth() / -2) {
                    sprite.setX(sprite.getScreen().getWidth() / 2 - radius);
                  }
                  if (dy > 0 && sprite.getY() - radius > sprite.getScreen().getHeight() / 2) {
                    sprite.setY(-sprite.getScreen().getHeight() / 2 - radius);
                  } else if (dy < 0 && sprite.getY() + radius < sprite.getScreen().getHeight() / -2) {
                    sprite.setY(sprite.getScreen().getHeight() / 2 + radius);
                  }
                  break;
                case BOUNCE:
                  if (dx > 0 && sprite.getX() + radius > sprite.getScreen().getWidth() / 2 ||
                      dy > 0 && sprite.getY() + radius > sprite.getScreen().getHeight() / 2) {
                    direction.set(direction.get() + 90);
                  } else if (dx < 0 && sprite.getX() - radius < sprite.getScreen().getWidth() / -2
                      || dy < 0 && sprite.getY() - radius < sprite.getScreen().getHeight() / -2) {
                    direction.set(direction.get() + 90);
                  }
                  break;
              }
            }
        }
        double r = rotation.get();
        if (r != 0.0) {
            angle.set(angle.get() + r * dt / 1000);
        }
        collisions.invalidate();
    }


    class NumberProperty extends Property<Double> {
        private final SpriteMetaProperty target;

        NumberProperty(SpriteMetaProperty target) {
            this.target = target;
        }

        @Override
        public Double get() {
            switch (target) {
                case x:
                    return (double) sprite.getX();
                case y:
                    return (double) sprite.getY();
                case z:
                    return (double) sprite.getZ();
                case angle:
                    return (double) sprite.getAngle();
                case size:
                    return (double) sprite.getSize();
                case left:
                    return sprite.getX() - (sprite.getAnchor().getWidthForAnchoring() + sprite.getSize()) / 2.0;
                case right:
                    return -sprite.getX() - (sprite.getAnchor().getWidthForAnchoring() + sprite.getSize()) / 2.0;
                case top:
                    return -sprite.getY() - (sprite.getAnchor().getHeightForAnchoring() + sprite.getSize()) / 2.0;
                case bottom:
                    return sprite.getY() - (sprite.getAnchor().getHeightForAnchoring() + sprite.getSize()) / 2.0;
                case dx:
                    return speed.get() * Math.cos(Math.toRadians(90 - direction.get()));
                case dy:
                    return speed.get() * Math.sin(Math.toRadians(90 - direction.get()));

            }
            throw new RuntimeException();
        }

        boolean changeDxy(double dx, double dy) {
            double newSpeed = Math.sqrt(dx * dx + dy * dy);

            if (newSpeed == 0) {
                return speed.set(0.0);
            }

            double newDirection = 90 - Math.toDegrees(Math.atan2(dy, dx));

            return speed.set(newSpeed) | direction.set(newDirection < 0 ? 360 + newDirection : newDirection);
        }

        @Override
        public boolean setImpl(Double value) {
            switch (target) {
                case x:
                    if (!sprite.setX(value.floatValue())) {
                        return false;
                    }
                    left.notifyChanged();
                    right.notifyChanged();
                    return true;
                case y:
                    if (!sprite.setY(value.floatValue())) {
                        return false;
                    }
                    top.notifyChanged();
                    bottom.notifyChanged();
                    return true;
                case dx:
                    return changeDxy(value, dy.get());
                case dy:
                    return changeDxy(dx.get(), value);
                case z:
                    return sprite.setZ(value.floatValue());
                case angle:
                    return sprite.setAngle(value.floatValue());
                case size:
                        return sprite.setSize(value.floatValue());
                case left:
                    return sprite.setX(value.floatValue() + (sprite.getSize() + sprite.getAnchor().getWidthForAnchoring()) / 2);
                case right:
                    return sprite.setX(-(value.floatValue() + (sprite.getAnchor().getWidthForAnchoring() + sprite.getSize()) / 2));
                case top:
                    return sprite.setY(-(value.floatValue() + (sprite.getAnchor().getHeightForAnchoring() + sprite.getSize()) / 2));
                case bottom:
                    return sprite.setY(value.floatValue() + (sprite.getSize() + sprite.getAnchor().getHeightForAnchoring()) / 2);
            }
            throw new RuntimeException();
        }

    }

    class ObjectProperty extends Property<Object> {
        private final SpriteMetaProperty target;

        ObjectProperty(SpriteMetaProperty target) {
            this.target = target;
        }

        public Object get() {
            switch (target) {
                case bubble: {
                    Object result = sprite.getBubble().getTag();
                    return result != null ? result : new TextBoxAdapter(sprite.getBubble());
                }
                case face:
                    return sprite.getFace();
                case label: {
                    Object result = sprite.getLabel().getTag();
                    return result != null ? result : new TextBoxAdapter(sprite.getBubble());
                }
                case anchor:
                    return sprite.getAnchor().getTag();
            }
            throw new RuntimeException();
        }

        public boolean setImpl(Object value) {
            switch (target) {
                case bubble: return sprite.setBubble(((TextBoxAdapter) value).textBox);
                case label: return sprite.setLabel(((TextBoxAdapter) value).textBox);
                case face:return sprite.setFace((String) value);
                case anchor: return sprite.setAnchor(((SpriteAdapter) value).sprite);
            }
            throw new RuntimeException();
        }
    }


    enum SpriteMetaProperty implements PropertyDescriptor {
        x(Types.NUMBER), y(Types.NUMBER), z(Types.NUMBER), size(Types.NUMBER),
        left(Types.NUMBER), right(Types.NUMBER), top(Types.NUMBER), bottom(Types.NUMBER),
        speed(Types.NUMBER), direction(Types.NUMBER), dx(Types.NUMBER), dy(Types.NUMBER),
        angle(Types.NUMBER), label(TextBoxAdapter.CLASSIFIER), bubble(TextBoxAdapter.CLASSIFIER), face(Types.STRING),
        rotation(Types.NUMBER), collisions(new ArrayType(SpriteAdapter.CLASSIFIER)),
        anchor(SpriteAdapter.CLASSIFIER), edgeMode(EDGE_MODE),
        say(new FunctionType(Types.VOID, Types.STRING));

        final Type type;

        SpriteMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
