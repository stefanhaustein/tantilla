package org.kobjects.asde.android.library.ui;

import org.kobjects.asde.lang.classifier.NativeClass;
import org.kobjects.asde.lang.classifier.NativeMethod;
import org.kobjects.asde.lang.classifier.NativeProperty;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.graphics.Pen;


public class PenType {

  public static NativeClass TYPE = new NativeClass("Pen", "A pen");
  static {
    Types.addClass(Pen.class, TYPE);
    TYPE.addProperties(
        new NativeProperty("fillColor", "The current fill color for this pen.", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((Pen) instance).getFillColor();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((Pen) instance).setFillColor((int) ((Double) value).longValue());
          }
        },
        new NativeProperty("strokeColor", "The current line color for this pen.", Types.FLOAT) {
           @Override
           public Object get(EvaluationContext context, Object instance) {
              return (double) ((Pen) instance).getLineColor();
           }

           @Override
           public void set(EvaluationContext context, Object instance, Object value) {
             ((Pen) instance).setLineColor((int) ((Double) value).longValue());
           }
        },
        new NativeProperty("textSize", "The text size.", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((Pen) instance).getTextSize();
          }

          @Override
          public void set(EvaluationContext context, Object instance, Object value) {
            ((Pen) instance).setTextSize(((Double) value).floatValue());
          }
        },
        new NativeMethod(
            "clear",
            "Clear the rectangle determined by the four coordinates",
            Types.VOID, TYPE,
            Types.FLOAT, Types.FLOAT, Types.FLOAT, Types.FLOAT) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Pen pen = (Pen) evaluationContext.getParameter(0);
            pen.clearRect(
                ((Double) evaluationContext.getParameter(1)).floatValue(),
                ((Double) evaluationContext.getParameter(2)).floatValue(),
                ((Double) evaluationContext.getParameter(3)).floatValue(),
                ((Double) evaluationContext.getParameter(4)).floatValue());
            return null;
          }
        },
        new NativeMethod(
            "rect",
            "Draw the rectangle determined by the four coordinates",
            Types.VOID, TYPE,
            Types.FLOAT, Types.FLOAT, Types.FLOAT, Types.FLOAT) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Pen pen = (Pen) evaluationContext.getParameter(0);
            pen.drawRect(
                ((Double) evaluationContext.getParameter(1)).floatValue(),
                ((Double) evaluationContext.getParameter(2)).floatValue(),
                ((Double) evaluationContext.getParameter(3)).floatValue(),
                ((Double) evaluationContext.getParameter(4)).floatValue());
            return null;
          }
        },
        new NativeMethod(
            "line",
            "Draw the line determined by the four coordinates",
            Types.VOID, TYPE,
            Types.FLOAT, Types.FLOAT, Types.FLOAT, Types.FLOAT) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Pen pen = (Pen) evaluationContext.getParameter(0);
            pen.drawLine(
                ((Double) evaluationContext.getParameter(1)).floatValue(),
                ((Double) evaluationContext.getParameter(2)).floatValue(),
                ((Double) evaluationContext.getParameter(3)).floatValue(),
                ((Double) evaluationContext.getParameter(4)).floatValue());
            return null;
          }
        },
        new NativeMethod(
            "write",
            "Write the given text at the given coordinates",
            Types.VOID, TYPE,
            Types.FLOAT, Types.FLOAT, Types.STR) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            Pen pen = (Pen) evaluationContext.getParameter(0);
            pen.drawText(
                ((Double) evaluationContext.getParameter(1)).floatValue(),
                ((Double) evaluationContext.getParameter(2)).floatValue(),
                ((String) evaluationContext.getParameter(3)));
            return null;
          }
        });
  }

}
