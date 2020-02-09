package org.kobjects.asde.android.library.ui;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.classifier.Method;
import org.kobjects.asde.lang.type.ChangeListener;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.graphics.Animated;
import org.kobjects.graphics.EdgeMode;
import org.kobjects.graphics.Sprite;
import org.kobjects.graphics.XAlign;
import org.kobjects.graphics.YAlign;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.classifier.InstanceTypeImpl;
import org.kobjects.asde.lang.property.LazyProperty;
import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;

public class SpriteAdapter extends Instance implements Animated {
  public static EnumType EDGE_MODE = Types.wrapEnum("EdgeMode", EdgeMode.values());

  public static final InstanceTypeImpl TYPE =
      new InstanceTypeImpl("Sprite",
          "Class representing character objects on the screen.") {
        @Override
        public boolean supportsChangeListeners() {
          return true;
        }

        @Override
        public void addChangeListener(final Object instance, Runnable changeListener) {
          ((SpriteAdapter) instance).sprite.addChangeListener(unused -> changeListener.run());
        }
      };

  static {
    TYPE.addProperties(SpriteAdapter.SpriteMetaProperty.values());
  }


  final Sprite sprite;

  // Potentially animated properties

  final WriteThroughProperty x = new WriteThroughProperty(SpriteMetaProperty.x);
  final WriteThroughProperty y = new WriteThroughProperty(SpriteMetaProperty.y);
  final WriteThroughProperty z = new WriteThroughProperty(SpriteMetaProperty.z);
  final WriteThroughProperty size = new WriteThroughProperty(SpriteMetaProperty.size);
  final WriteThroughProperty angle = new WriteThroughProperty(SpriteMetaProperty.angle);
  final WriteThroughProperty opacity = new WriteThroughProperty(SpriteMetaProperty.opacity);

  // Animation properties

  final WriteThroughProperty dx = new WriteThroughProperty(SpriteMetaProperty.dx);
  final WriteThroughProperty dy = new WriteThroughProperty(SpriteMetaProperty.dy);
  final WriteThroughProperty speed = new WriteThroughProperty(SpriteMetaProperty.speed);
  final WriteThroughProperty rotation = new WriteThroughProperty(SpriteMetaProperty.rotation);
  final WriteThroughProperty grow = new WriteThroughProperty(SpriteMetaProperty.grow);
  final WriteThroughProperty fade = new WriteThroughProperty(SpriteMetaProperty.fade);
  final WriteThroughProperty direction = new WriteThroughProperty(SpriteMetaProperty.direction);

  // Other properties

  final ObjectProperty bubble = new ObjectProperty(SpriteMetaProperty.bubble);
  final ObjectProperty label = new ObjectProperty(SpriteMetaProperty.label);
  final ObjectProperty face = new ObjectProperty(SpriteMetaProperty.face);
  final ObjectProperty anchor = new ObjectProperty(SpriteMetaProperty.anchor);
  final ObjectProperty edgeMode = new ObjectProperty(SpriteMetaProperty.edgeMode);
  final ObjectProperty xAlign = new ObjectProperty(SpriteMetaProperty.xAlign);
  final ObjectProperty yAlign = new ObjectProperty(SpriteMetaProperty.yAlign);

  final ObjectProperty collisions = new ObjectProperty(SpriteMetaProperty.collisions);
  ListImpl collisionsArray = new ListImpl(SpriteAdapter.TYPE, 0);

  public SpriteAdapter(final ScreenAdapter screen) {
    super(TYPE);
    sprite = new Sprite(screen.getScreen());
    sprite.setTag(this);
    sprite.setSize(10);

    speed.addListener(p -> {dx.notifyChanged(); dy.notifyChanged();});
    direction.addListener(p -> {dx.notifyChanged(); dy.notifyChanged();});
   }

  @Override
  public Property getProperty(PropertyDescriptor property) {
    switch ((SpriteMetaProperty) property) {
      case dx: return dx;
      case dy: return dy;
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
      case grow: return grow;
      case opacity: return opacity;
      case fade: return fade;
      case say: return new Method((FunctionType) SpriteMetaProperty.say.type()) {
        @Override
        public Object call(EvaluationContext evaluationContext, int paramCount) {
          sprite.say((String) (evaluationContext.getParameter(1)));
          return null;
        }};
      case x: return x;
      case y: return y;
      case z: return z;
      case xAlign: return xAlign;
      case yAlign: return yAlign;
    }
    throw new IllegalArgumentException();
  }

  @Override
  public void animate(float dt, boolean propertiesChanged) {
    Collection<Sprite> newCollisions = sprite.collisions();
    synchronized (collisionsArray) {
      for (int i = collisionsArray.length() - 1; i >= 0; i--) {
        SpriteAdapter oldAdapter = (SpriteAdapter) collisionsArray.get(i);
        if (!newCollisions.contains(oldAdapter.sprite)) {
          synchronized (this) {
            collisionsArray.remove(i);
          }
        }
      }
      for (Sprite colliding : newCollisions) {
        Object newAdapter = colliding.getTag();
        if (newAdapter instanceof SpriteAdapter && !collisionsArray.contains(newAdapter)) {
          synchronized (this) {
            collisionsArray.append(newAdapter);
          }
        }
      }
    }

    if (propertiesChanged) {
      x.invalidate();
      y.invalidate();
      angle.invalidate();
      opacity.invalidate();
    }
  }

  class WriteThroughProperty extends LazyProperty<Double> {
    private final SpriteMetaProperty target;

    WriteThroughProperty(SpriteMetaProperty target) {
      this.target = target;
    }

    @Override
    protected Double compute() {
      switch (target) {
        case dx:
          return (double) sprite.getDx();
        case dy:
          return (double) sprite.getDy();

        case speed:
          return (double) sprite.getSpeed();
        case direction:
          return (double) sprite.getDirection();
        case grow:
          return (double) sprite.getGrow();
        case fade:
          return (double) sprite.getFade();
        case rotation:
          return (double) sprite.getRotation();
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
        case opacity:
          return (double) sprite.getOpacity();

      }
      throw new RuntimeException();
    }


    @Override
    public boolean setImpl(Double value) {
      float f = value.floatValue();
      switch (target) {
        case x:
          return sprite.setX(f);
        case y:
          return sprite.setY(f);
        case z:
          return sprite.setZ(f);
        case angle:
          return sprite.setAngle(f);
        case opacity:
          return sprite.setOpacity(f);
        case size:
          if (sprite.setSize(f)) {
            x.invalidate();
            y.invalidate();
            return true;
          }
          return false;
        case dx:
          if (sprite.setDx(f)) {
            speed.invalidate();
            direction.invalidate();
            return true;
          }
          return false;
        case dy:
          if (sprite.setDy(f)) {
            speed.invalidate();
            direction.invalidate();
            return true;
          }
          return false;
        case direction:
          if (sprite.setDirection(f)) {
            dx.invalidate();
            dy.invalidate();
            return true;
          }
          return false;
        case speed:
          if (sprite.setSpeed(f)) {
            dx.invalidate();
            dy.invalidate();
            return true;
          }
          return false;
        case grow:
          return sprite.setGrow(value.floatValue());
        case rotation:
          return sprite.setRotation(value.floatValue());
        case fade:
          return sprite.setFade(value.floatValue());

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
        case edgeMode:
          return sprite.getEdgeMode();
        case xAlign:
          return sprite.getXAlign();
        case yAlign:
          return sprite.getYAlign();
        case collisions:
          return collisionsArray;
      }
      throw new RuntimeException();
    }

    public boolean setImpl(Object value) {
      switch (target) {
        case anchor: return sprite.setAnchor(((SpriteAdapter) value).sprite);
        case bubble: return sprite.setBubble(((TextBoxAdapter) value).textBox);
        case edgeMode: return sprite.setEdgeMode((EdgeMode) value);
        case face:return sprite.setFace((String) value);
        case label: return sprite.setLabel(((TextBoxAdapter) value).textBox);
        case xAlign: return sprite.setXAlign((XAlign) value);
        case yAlign: return sprite.setYAlign((YAlign) value);
      }
      throw new RuntimeException();
    }
  }

  enum SpriteMetaProperty implements PropertyDescriptor {
    x(Types.FLOAT),
    y(Types.FLOAT),
    z(Types.FLOAT),
    xAlign(ScreenAdapter.X_ALIGN),
    yAlign(ScreenAdapter.Y_ALIGN),
    size(Types.FLOAT),
    opacity(Types.FLOAT),
    speed(Types.FLOAT),
    direction(Types.FLOAT),
    dx(Types.FLOAT),
    dy(Types.FLOAT),
    grow(Types.FLOAT),
    fade(Types.FLOAT),
    angle(Types.FLOAT),
    label(TextBoxAdapter.TYPE),
    bubble(TextBoxAdapter.TYPE),
    face(Types.STR),
    rotation(Types.FLOAT),
    collisions(new ListType(SpriteAdapter.TYPE)),
    anchor(SpriteAdapter.TYPE),
    edgeMode(EDGE_MODE),
    say(new FunctionType(Types.VOID, TYPE, Types.STR));

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
