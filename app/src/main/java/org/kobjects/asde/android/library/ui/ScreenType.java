package org.kobjects.asde.android.library.ui;

import org.kobjects.asde.lang.classifier.NativeMethodDescriptor;
import org.kobjects.asde.lang.classifier.NativeReadonlyPropertyDescriptor;
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
        new NativeReadonlyPropertyDescriptor("width", "The width of the visible area. At least 200 and exactly 200 for a square screen.", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((Screen) instance).getWidth();
          }
        },
        new NativeReadonlyPropertyDescriptor("height", "The height of the visible area. At least 200 and exactly 200 for a square screen.", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return ((Screen) instance).getWidth();
          }
        },
        new NativeMethodDescriptor("newPen", "Create a new pen drawing to this screen object.", PenType.TYPE, TYPE) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Screen self = (Screen) evaluationContext.getParameter(0);
            return new Pen(self);
          }
        },
        new NativeMethodDescriptor("newSprite", "Create a new sprite attached to this screen object.", SpriteAdapter.TYPE, TYPE) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Screen self = (Screen) evaluationContext.getParameter(0);
            return new SpriteAdapter(self);
          }
        },
        new NativeMethodDescriptor("newTextBox", "Create a new textBox attached to this screen object.", TextBoxType.TYPE, TYPE) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Screen self = (Screen) evaluationContext.getParameter(0);
            return new TextBox(self);
          }
        }
    );
  }

}
