package org.kobjects.asde.android.library.ui;

import android.view.View;

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
import org.kobjects.graphics.Screen;
import org.kobjects.graphics.XAlign;
import org.kobjects.graphics.YAlign;


public class ScreenAdapter extends Instance implements View.OnLayoutChangeListener{

  public static EnumType X_ALIGN = Types.wrapEnum("XAlign", XAlign.values());
  public static EnumType Y_ALIGN = Types.wrapEnum("YAlign", YAlign.values());

  private final Screen screen;
  private float scale;

  // Hack; access from viewport instead.
  float width;
  float height;

  final PhysicalProperty<Double> widthProperty = new PhysicalProperty<>(0.0);
  final PhysicalProperty<Double> heightProperty = new PhysicalProperty<>(0.0);

  public static final InstanceTypeImpl TYPE = new InstanceTypeImpl("Screen (Singleton)",
    "Representation of the main device screen.");

  static {
    TYPE.addProperties(ScreenMetaProperty.values());
  }

    public ScreenAdapter(Screen screen) {
        super(TYPE);
        this.screen = screen;
        screen.view.addOnLayoutChangeListener(this);
    }

    public void cls() {
        screen.cls();
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((ScreenMetaProperty) property) {
            case width: return widthProperty;
            case height: return heightProperty;
            case newPen: return new Method((FunctionType) ScreenMetaProperty.newPen.type) {
                @Override
                public Object call(EvaluationContext evaluationContext, int paramCount) {
                    return new PenAdapter(screen.createPen());
                }
            };
            case newSprite: return new Method((FunctionType) ScreenMetaProperty.newSprite.type) {
                @Override
                public Object call(EvaluationContext evaluationContext, int paramCount) {
                    return new SpriteAdapter(ScreenAdapter.this);
                }
            };
            case newTextBox: return new Method((FunctionType) ScreenMetaProperty.newTextBox.type) {
                @Override
                public Object call(EvaluationContext evaluationContext, int paramCount) {
                    return new TextBoxAdapter(ScreenAdapter.this);
                }
            };
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int widthPx = right - left;
        int heightPx = bottom - top;

        scale = Math.min(widthPx, heightPx) / 200f;

        width = widthPx / scale;
        height = heightPx / scale;

        widthProperty.set(Double.valueOf(width));
        heightProperty.set(Double.valueOf(height));
    }

    public Screen getScreen() {
        return screen;
    }

    private enum ScreenMetaProperty implements PropertyDescriptor {
        width(Types.FLOAT), height(Types.FLOAT),
        newPen(new FunctionType(PenAdapter.TYPE)),
        newSprite(new FunctionType(SpriteAdapter.TYPE)),
        newTextBox(new FunctionType(TextBoxAdapter.TYPE));

        private final Type type;

        ScreenMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
