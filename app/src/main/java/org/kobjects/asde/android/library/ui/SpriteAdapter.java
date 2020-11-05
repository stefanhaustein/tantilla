package org.kobjects.asde.android.library.ui;

import org.kobjects.asde.lang.classifier.builtin.NativeMethod;
import org.kobjects.asde.lang.classifier.builtin.NativeProperty;
import org.kobjects.asde.lang.classifier.builtin.NativeReadonlyProperty;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.type.Typed;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.krash.android.AndroidSprite;
import org.kobjects.krash.api.Animated;
import org.kobjects.krash.api.Content;
import org.kobjects.krash.api.EdgeMode;
import org.kobjects.krash.api.Screen;
import org.kobjects.krash.api.Sprite;
import org.kobjects.krash.api.TextContent;
import org.kobjects.krash.api.XAlign;
import org.kobjects.krash.api.YAlign;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.classifier.builtin.NativeClass;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.krash.api.Animated;

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
        new NativeProperty(TYPE, "x", "x-position", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getX();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setX(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "y", "y-position", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getY();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setY(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "z", "z-position", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getZ();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setZ(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "width", "width", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getWidth();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setWidth(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "height", "height", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getHeight();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setHeight(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "angle", "angle", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getAngle();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setAngle(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "opacity", "Sprite opacity ranging from 0 to 1", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getOpacity();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setOpacity(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "dx", "x-component of the velocity", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getDx();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setDx(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "dy", "y-component of the velocity", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getDy();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setDy(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "speed", "speed", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getSpeed();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setSpeed(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "rotation", "rotation speed", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getRotation();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setRotation(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "grow", "growth", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getGrow();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setGrow(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "fade", "fading speed", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getFade();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setFade(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "direction", "Moving direction", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((SpriteAdapter) instance).sprite.getDirection();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setDirection(((Double) value).floatValue());
          }
        },
        new NativeProperty(TYPE, "bubble", "Text bubble", SpriteAdapter.TYPE) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getBubble();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setBubble(((Sprite) value));
          }
        },
        new NativeProperty(TYPE, "label", "Text label", SpriteAdapter.TYPE) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getLabel();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setLabel(((Sprite) value));
          }
        },

        new NativeProperty(TYPE, "face", "Sprite Emoji face", Types.STR) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getFace();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setFace((String) value);
          }
        },

        new NativeProperty(TYPE, "text", "Sprite text", Types.STR) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            Content content = ((SpriteAdapter) instance).sprite.getContent();
            return (content instanceof TextContent) ? ((TextContent) content).getText() : "";
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            Sprite sprite = ((SpriteAdapter) instance).sprite;
            sprite.setContent(sprite.getScreen().createText(String.valueOf(value)));
          }
        },

        new NativeProperty(TYPE, "anchor", "Anchor for relative positioning", TYPE) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getAnchor().getTag();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setAnchor(((SpriteAdapter) value).sprite);
          }
        },

        new NativeProperty(TYPE, "edgeMode", "EdgeMode", EDGE_MODE) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getEdgeMode();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setEdgeMode((EdgeMode) value);
          }
        },

        new NativeProperty(TYPE, "xAlign", "X-Align", ScreenType.X_ALIGN) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getXAlign();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setXAlign((XAlign) value);
          }
        },

        new NativeProperty(TYPE, "yAlign", "Y-Align", ScreenType.Y_ALIGN) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).sprite.getYAlign();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((SpriteAdapter) instance).sprite.setYAlign((YAlign) value);
          }
        },

        new NativeReadonlyProperty(TYPE, "collisions", "List of colliding sprites", new ListType(TYPE)) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((SpriteAdapter) instance).collisionsArray;
          }
        },

        new NativeMethod(TYPE, "say", "Creats a bubble with the given text.", Types.VOID, TYPE, Types.STR) {
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
    sprite = screen.createSprite();
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
