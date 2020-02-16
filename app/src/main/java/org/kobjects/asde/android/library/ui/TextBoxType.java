package org.kobjects.asde.android.library.ui;

import org.kobjects.asde.lang.classifier.NativeProperty;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.graphics.TextBox;
import org.kobjects.graphics.XAlign;
import org.kobjects.graphics.YAlign;
import org.kobjects.asde.lang.classifier.NativeClass;

public class TextBoxType {

    public static final NativeClass TYPE =new NativeClass("TextBox",
        "Class representing a box of text on the screen.");
    static {
        Types.addClass(TextBox.class, TYPE);
        TYPE.addProperties(
            new NativeProperty("x", "x-position", Types.FLOAT) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return (double) ((TextBox) instance).getX();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setX(((Double) value).floatValue());
                }
            },
            new NativeProperty("y", "y-position", Types.FLOAT) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return (double) ((TextBox) instance).getY();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setY(((Double) value).floatValue());
                }
            },
            new NativeProperty("z", "z-position", Types.FLOAT) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return (double) ((TextBox) instance).getZ();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setZ(((Double) value).floatValue());
                }
            },
            new NativeProperty("size", "size", Types.FLOAT) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return (double) ((TextBox) instance).getSize();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setSize(((Double) value).floatValue());
                }
            },
            new NativeProperty("cornerRadius", "Radius of the text box corners.", Types.FLOAT) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return (double) ((TextBox) instance).getCornerRadius();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setCornerRadius(((Double) value).floatValue());
                }
            },
            new NativeProperty("lineWidth", "Width of the border line.", Types.FLOAT) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return (double) ((TextBox) instance).getLineWidth();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setLineWidth(((Double) value).floatValue());
                }
            },

            new NativeProperty("lineColor", "Color used for the text box outline", Types.FLOAT) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return (double) ((TextBox) instance).getLineColor();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setLineColor((int) ((Double) value).doubleValue());
                }
            },
            new NativeProperty("textColor", "Color used for the text content", Types.FLOAT) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return (double) ((TextBox) instance).getTextColor();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setTextColor((int) ((Double) value).doubleValue());
                }
            },
            new NativeProperty("fillColor", "Color used to fill the text box background", Types.FLOAT) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return (double) ((TextBox) instance).getFillColor();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setFillColor((int) ((Double) value).doubleValue());
                }
            },

            new NativeProperty("anchor", "Anchor for relative positioning", TYPE) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return ((TextBox) instance).getAnchor().getTag();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setAnchor(((SpriteAdapter) value).sprite);
                }
            },

            new NativeProperty("xAlign", "X-Align", ScreenType.X_ALIGN) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return ((TextBox) instance).getXAlign();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setXAlign((XAlign) value);
                }
            },

            new NativeProperty("yAlign", "Y-Align", ScreenType.Y_ALIGN) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return ((TextBox) instance).getYAlign();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setYAlign((YAlign) value);
                }
            },

            new NativeProperty("text", "Sprite Emoji face", Types.STR) {
                @Override
                public Object get(EvaluationContext context, Object instance) {
                    return ((TextBox) instance).getText();
                }

                @Override
                public void set(EvaluationContext context, Object instance, Object value) {
                    ((TextBox) instance).setText((String) value);
                }
            }
        );
    }


}
