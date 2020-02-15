package org.kobjects.asde.android.library.ui;

import org.kobjects.asde.lang.classifier.NativeMethodDescriptor;
import org.kobjects.asde.lang.classifier.NativePropertyDescriptor;
import org.kobjects.asde.lang.classifier.NativeReadonlyPropertyDescriptor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.type.Typed;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.graphics.Animated;
import org.kobjects.graphics.EdgeMode;
import org.kobjects.graphics.Screen;
import org.kobjects.graphics.Sprite;
import org.kobjects.graphics.TextBox;
import org.kobjects.graphics.XAlign;
import org.kobjects.graphics.YAlign;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.classifier.NativeClass;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;

public class SpriteAdapter implements Animated, Typed {
  public static EnumType EDGE_MODE = Types.wrapEnum("EdgeMode", EdgeMode.values());

  public static final NativeClass TYPE =
      new NativeClass("Sprite",
          "Class representing character objects on the screen.") {
        @Override
        public boolean supportsChangeListeners() {
          return true;
        }

        @Override
        public void addChangeListener(final Object instance, Runnable listener) {
          ((SpriteAdapter) instance).sprite.addChangeListener(listener);
        }
      };

  static {
    TYPE.addProperties(
        new NativePropertyDescriptor("x", "x-position", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getX();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setX(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("y", "y-position", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getY();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setY(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("z", "z-position", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getZ();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setZ(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("size", "size", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getSize();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setSize(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("angle", "angle", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getAngle();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setAngle(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("opacity", "Sprite opacity ranging from 0 to 1", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getOpacity();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setOpacity(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("dx", "x-component of the velocity", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getDx();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setDx(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("dy", "y-component of the velocity", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getDy();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setDy(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("speed", "speed", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getSpeed();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setSpeed(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("rotation", "rotation speed", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getRotation();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setRotation(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("grow", "growth", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getGrow();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setGrow(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("fade", "fading speed", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getFade();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setFade(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("direction", "Moving direction", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getDirection();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setDirection(((Double) value).floatValue());
          }
        },
        new NativePropertyDescriptor("bubble", "Text bubble", TextBoxType.TYPE) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getBubble();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setBubble(((TextBox) value));
          }
        },
        new NativePropertyDescriptor("label", "Text label", TextBoxType.TYPE) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getLabel();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setLabel(((TextBox) value));
          }
        },

        new NativePropertyDescriptor("face", "Sprite Emoji face", Types.STR) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getFace();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setFace((String) value);
          }
        },
        new NativePropertyDescriptor("anchor", "Anchor for relative positioning", TYPE) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getAnchor().getTag();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setAnchor(((SpriteAdapter) value).sprite);
          }
        },

        new NativePropertyDescriptor("edgeMode", "EdgeMode", EDGE_MODE) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getEdgeMode();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setEdgeMode((EdgeMode) value);
          }
        },

        new NativePropertyDescriptor("xAlign", "X-Align", ScreenType.X_ALIGN) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getXAlign();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setXAlign((XAlign) value);
          }
        },

        new NativePropertyDescriptor("yAlign", "Y-Align", ScreenType.Y_ALIGN) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getYAlign();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setYAlign((YAlign) value);
          }
        },

        new NativeReadonlyPropertyDescriptor("collisions", "List of colliding sprites", new ListType(TYPE)) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).collisionsArray;
          }
        },

        new NativeMethodDescriptor("say", "Creats a bubble with the given text.", Types.VOID, TYPE, Types.STR) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            SpriteAdapter self = (SpriteAdapter) evaluationContext.getParameter(0);
            self.sprite.say((String) evaluationContext.getParameter(1));
            return null;
          }
        }
    );
  }


  final Sprite sprite;

  ListImpl collisionsArray = new ListImpl(SpriteAdapter.TYPE);

  public SpriteAdapter(final Screen screen) {
    sprite = new Sprite(screen);
    sprite.setTag(this);
    sprite.setSize(10);
   }


  @Override
  public void animate(float dt, boolean propertiesChanged) {
    Collection<Sprite> newCollisions = sprite.collisions();
    synchronized (collisionsArray) {
      int added = 0;
      int removed = 0;
      for (int i = collisionsArray.length() - 1; i >= 0; i--) {
        SpriteAdapter oldAdapter = (SpriteAdapter) collisionsArray.get(i);
        if (!newCollisions.contains(oldAdapter.sprite)) {
          synchronized (this) {
            collisionsArray.remove(i);
            removed++;
          }
        }
      }
      for (Sprite colliding : newCollisions) {
        Object newAdapter = colliding.getTag();
        if (newAdapter instanceof SpriteAdapter && !collisionsArray.contains(newAdapter)) {
          synchronized (this) {
            collisionsArray.append(newAdapter);
            added++;
          }
        }
      }
      if (added != 0 || removed != 0) {
        System.out.println("addedd: " + added + " removed: " + removed + " face: " + sprite.getFace());
      }
    }
  }

  @Override
  public Type getType() {
    return TYPE;
  }


}
