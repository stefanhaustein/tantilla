package org.kobjects.asde.android.library.ui;

import android.view.MotionEvent;
import android.widget.ImageView;

import org.kobjects.asde.lang.classifier.builtin.NativeProperty;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Typed;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.krash.Dpad;
import org.kobjects.asde.lang.classifier.builtin.NativeClass;
import org.kobjects.asde.lang.type.Type;


import java.util.ArrayList;

public class DpadAdapter implements Typed {

  Object lock = new Object();

  static String[] BUTTON_NAMES = {"up", "down", "left", "right", "fire"};

  static NativeClass TYPE = new NativeClass("Dpad (Singleton)",
      "Virtual directional pad that is displayed at the bottom of the screen"
          + "when the visible property is set. Other properties are true when the "
          + "corresponding key is pressed.") {
    @Override
    public boolean supportsChangeListeners() {
      return true;
    }

    @Override
    public void addChangeListener(final Object instance, Runnable changeListener) {
      ((DpadAdapter) instance).addChangeListener(changeListener);
    }

  };
  static {
    Types.addClass(DpadAdapter.class, TYPE);
    for (int i = 0; i < BUTTON_NAMES.length; i++) {
      final int index = i;
      TYPE.addProperties(
          new NativeProperty(TYPE, BUTTON_NAMES[i], "True if the '"+ BUTTON_NAMES[i] + "' button is pressed", Types.BOOL) {
            @Override
            public Object get(EvaluationContext context, Object instance) {
              return ((DpadAdapter) instance).buttonState[index];
            }

            @Override
            public void set(EvaluationContext context, Object instance, Object value) {
              ((DpadAdapter) instance).buttonState[index] = (Boolean) value;
            }
          });
    }
    TYPE.addProperties(
        new NativeProperty(TYPE, "visible", "True if the dpad is currently shown.", Types.BOOL) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((DpadAdapter) instance).dpad.getVisible();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((DpadAdapter) instance).dpad.setVisible((Boolean) value);
          }
        });
  }


  final Dpad dpad;
  private final ArrayList<Runnable> changeListeners = new ArrayList<>();
  boolean[] buttonState = new boolean[5];


  public DpadAdapter(final Dpad dpad) {
    this.dpad = dpad;
    ImageView[] buttons = {dpad.up, dpad.down, dpad.left, dpad.right, dpad.fire};

    for (int i = 0; i < buttons.length; i++) {
      final int index = i;
      buttons[i].setOnTouchListener((view, event) -> {
        boolean newState;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          newState = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
          newState = false;
        } else {
          return false;
        }
        if (buttonState[index] == newState) {
          return false;
        }
        buttonState[index] = newState;
        DpadAdapter.this.notifyChanged();
        return true;
      });
    }
  }


  public void addChangeListener(Runnable changeListener) {
    synchronized (lock) {
      changeListeners.add(changeListener);
    }
  }

  @Override
  public Type getType() {
    return TYPE;
  }

  public void notifyChanged() {
    Runnable[] snapshot;
    synchronized (lock) {
      snapshot = changeListeners.toArray(new Runnable[0]);
    }
    for (Runnable changeListener : snapshot) {
      changeListener.run();
    }
  }

  public void removeAllListeners() {
    synchronized (lock) {
      changeListeners.clear();
    }
  }
}
