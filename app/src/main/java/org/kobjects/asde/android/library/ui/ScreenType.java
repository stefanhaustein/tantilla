package org.kobjects.asde.android.library.ui;

import org.kobjects.asde.lang.classifier.NativeMethod;
import org.kobjects.asde.lang.classifier.NativeReadonlyProperty;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.classifier.NativeClass;
import org.kobjects.graphics.Pen;
import org.kobjects.graphics.Screen;
import org.kobjects.graphics.TextBox;
import org.kobjects.graphics.XAlign;
import org.kobjects.graphics.YAlign;


public class ScreenType {

  public static EnumType X_ALIGN = Types.wrapEnum("XAlign", XAlign.values());
  public static EnumType Y_ALIGN = Types.wrapEnum("YAlign", YAlign.values());


  public static final NativeClass TYPE = new NativeClass("Screen (Singleton)",
    "Representation of the main device screen.");

  static {
    Types.addClass(Screen.class, TYPE);
    TYPE.addProperties(
        new NativeReadonlyProperty(TYPE, "width", "The width of the visible area. At least 200 and exactly 200 for a square screen.", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((Screen) instance).getWidth();
          }
        },
        new NativeReadonlyProperty(TYPE, "height", "The height of the visible area. At least 200 and exactly 200 for a square screen.", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((Screen) instance).getHeight();
          }
        },
        new NativeMethod(TYPE, "newPen", "Create a new pen drawing to this screen object.", PenType.TYPE, TYPE) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Screen self = (Screen) evaluationContext.getParameter(0);
            return new Pen(self);
          }
        },
        new NativeMethod(TYPE, "newSprite", "Create a new sprite attached to this screen object.", SpriteAdapter.TYPE, TYPE) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Screen self = (Screen) evaluationContext.getParameter(0);
            return new SpriteAdapter(self);
          }
        },
        new NativeMethod(TYPE, "newTextBox", "Create a new textBox attached to this screen object.", TextBoxType.TYPE, TYPE) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Screen self = (Screen) evaluationContext.getParameter(0);
            return new TextBox(self);
          }
        },
        new NativeMethod(TYPE, "cls", "Clear the screen", Types.VOID, TYPE) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Screen self = (Screen) evaluationContext.getParameter(0);
            self.cls();
            return null;
          }
        }

    );
  }

}
