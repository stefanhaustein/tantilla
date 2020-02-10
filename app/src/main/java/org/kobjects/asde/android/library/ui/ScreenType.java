package org.kobjects.asde.android.library.ui;

import android.view.View;

import org.kobjects.asde.lang.property.MethodDescriptor;
import org.kobjects.asde.lang.property.NativePropertyDescriptor;
import org.kobjects.asde.lang.property.NativeReadonlyPropertyDescriptor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.classifier.Method;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.classifier.InstanceTypeImpl;
import org.kobjects.asde.lang.property.PhysicalProperty;
import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.graphics.Pen;
import org.kobjects.graphics.Screen;
import org.kobjects.graphics.XAlign;
import org.kobjects.graphics.YAlign;


public class ScreenType {

  public static EnumType X_ALIGN = Types.wrapEnum("XAlign", XAlign.values());
  public static EnumType Y_ALIGN = Types.wrapEnum("YAlign", YAlign.values());


  public static final InstanceTypeImpl TYPE = new InstanceTypeImpl("Screen (Singleton)",
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
        new MethodDescriptor("newPen", "Create a new pen drawing to this screen object.", PenType.TYPE, TYPE) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Screen self = (Screen) evaluationContext.getParameter(0);
            return new Pen(self);
          }
        },
        new MethodDescriptor("newSprite", "Create a new sprite attached to this screen object.", SpriteAdapter.TYPE, TYPE) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Screen self = (Screen) evaluationContext.getParameter(0);
            return new SpriteAdapter(self);
          }
        },
        new MethodDescriptor("newTextBox", "Create a new textBox attached to this screen object.", TextBoxAdapter.TYPE, TYPE) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Screen self = (Screen) evaluationContext.getParameter(0);
            return new TextBoxAdapter(self);
          }
        }
    );
  }

}
