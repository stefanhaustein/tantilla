package org.kobjects.asde.android.ide.help;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.method.LinkMovementMethod;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.AnnotatedStringConverter;
import org.kobjects.asde.lang.GlobalSymbol;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.type.Array;
import org.kobjects.asde.lang.type.ArrayType;
import org.kobjects.asde.lang.type.Function;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.typesystem.EnumType;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.InstanceType;
import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;


public class HelpDialog {

  public static void showHelp(MainActivity mainActivity, StaticSymbol symbol) {
    new HelpDialog(mainActivity).navigateTo(symbol);
  }


  public static void showHelp(MainActivity mainActivity) {
    new HelpDialog(mainActivity).navigateTo(null);
  }



  final MainActivity mainActivity;
  final LinearLayout linearLayout;
  final AlertDialog alertDialog;
  final ArrayList<Object> navigationStack = new ArrayList<>();


  HelpDialog(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
    linearLayout = new LinearLayout(mainActivity);
    linearLayout.setOrientation(LinearLayout.VERTICAL);
    linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    linearLayout.setDividerPadding(100);

    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
    alertBuilder.setTitle("Help");

    ScrollView scrollView = new ScrollView(mainActivity);
    scrollView.addView(linearLayout);

    ScrollView.LayoutParams params = (ScrollView.LayoutParams) linearLayout.getLayoutParams();
    params.setMargins(56, 56, 56, 56);


    alertBuilder.setView(scrollView);
    alertBuilder.setPositiveButton("Ok", null);
    /*  alertBuilder.setNegativeButton("Back", (a, b) -> {
      navigationStack.remove(navigationStack.size() - 1);
      updateContent();
    }); */

    alertDialog = alertBuilder.create();
    alertDialog.show();
  }

  void addLink(AnnotatedStringBuilder asb, Object linked) {
    if (linked instanceof StaticSymbol) {
      StaticSymbol symbol = (StaticSymbol) linked;
      if (symbol.getValue() instanceof Function) {
        addFunctionLink(asb, (StaticSymbol) linked);
      } else if (symbol.getValue() instanceof Type) {
        addLink(asb, symbol.getValue());
      } else {
        asb.append(symbol.getName());
        asb.append(": ");
        addLink(asb, symbol.getType());
      }
    } else if (linked instanceof PropertyDescriptor) {
      addPropertyLink(asb, (PropertyDescriptor) linked);
    } else {
      addLink(asb, String.valueOf(linked), linked);
    }
  }

  private void addPropertyLink(AnnotatedStringBuilder asb, PropertyDescriptor linked) {
    addLink(asb, linked.name(), linked);
    if (linked.type() instanceof FunctionType && !(linked.type() instanceof ArrayType)) {
      appendShortSignature(asb, (FunctionType) linked.type());
    } else {
      asb.append(": ");
      asb.append(String.valueOf(linked.type()));
    }

  }

  void addLink(AnnotatedStringBuilder asb, String text, Object linked) {
    asb.append(text, (Runnable) () -> navigateTo(linked));
  }

  void appendShortSignature(AnnotatedStringBuilder asb, FunctionType functionType) {
    asb.append('(');
    for (int i = 0; i < functionType.getParameterCount(); i++) {
      if (i > 0) {
        asb.append(", ");
      }
      asb.append(functionType.getParameterType(i).toString());
    }
    asb.append(")");

    if (functionType.getReturnType() != Types.VOID) {
      asb.append(" -> ").append(functionType.getReturnType().toString());
    }
  }

  void addFunctionLink(AnnotatedStringBuilder sb, StaticSymbol functionSymbol) {
    Function function = (Function) functionSymbol.getValue();

    addLink(sb, functionSymbol.getName(), functionSymbol);

    appendShortSignature(sb, function.getType());
  }



  HelpDialog navigateTo(Object o) {
    navigationStack.add(o);
    updateContent();
    return this;
  }

  void addAll(String subtitle, Predicate<StaticSymbol> filter) {
    addSubtitle(subtitle);

    for (StaticSymbol s : mainActivity.program.getSymbols()) {
      if (s.getScope() == GlobalSymbol.Scope.BUILTIN && filter.test(s)) {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        addLink(asb, s);
        addParagraph(asb.build());
      }
    }
  }

  HelpDialog updateContent() {
    linearLayout.removeAllViews();
    Object o = navigationStack.get(navigationStack.size() - 1);
    if (o == null) {
      alertDialog.setTitle("Help");
      linearLayout.removeAllViews();

      addAll("Constants", s -> !(s.getValue() instanceof Function) && !(s.getType() instanceof MetaType));

      addAll("Classes", s -> s.getValue() instanceof InstanceType);

      addAll("Enums", s -> s.getValue() instanceof EnumType);

      addAll("Functions", s -> s.getValue() instanceof Function);
    } else {
      renderObject(o);
    }

    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(navigationStack.size() > 1);
    return this;
  }

  private void addSubtitle(String text) {
    TextView textView = new TextView(mainActivity);
    textView.setText(text);
    textView.setPadding(0, 50, 0, 50);
    linearLayout.addView(textView);
  }


  void renderObject(Object o) {
    if (o instanceof StaticSymbol) {
      StaticSymbol symbol = (StaticSymbol) o;
      if (symbol.getValue() instanceof Function) {
        renderFunction(symbol);
      } else if (symbol.getValue() instanceof Type) {
        renderObject(symbol.getValue());
      } else {
        throw new RuntimeException("Don't know how to render " + symbol);
      }
    } else if (o instanceof InstanceType) {
      renderClass((InstanceType) o);
    } else if (o instanceof PropertyDescriptor) {
      renderProperty((PropertyDescriptor) o);
    } else if (o instanceof EnumType) {
      renderEnum((EnumType) o);
    } else {
      alertDialog.setTitle("Unknown");
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append(String.valueOf(o));
      addParagraph(asb.build());
    }
  }

  private void renderProperty(PropertyDescriptor o) {
    if (o.type() instanceof FunctionType && !(o.type() instanceof ArrayType)) {
      renderMethod(o);
    }

    alertDialog.setTitle("Property " + o.name());

    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    asb.append("Type: ");
    addLink(asb, o.type());

    addParagraph(asb.build());
  }

  private void renderMethod(PropertyDescriptor o) {
    alertDialog.setTitle("Method " + o.name());

    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();


  }


  void renderFunction(StaticSymbol functionSymbol) {
    Function function = (Function) functionSymbol.getValue();

    alertDialog.setTitle("Function " + functionSymbol.getName());

    AnnotatedStringBuilder sb = new AnnotatedStringBuilder();
    sb.append(functionSymbol.getName());
    appendShortSignature(sb, function.getType());

    addParagraph(sb.build());
  }


  void renderEnum(EnumType enumType) {
    alertDialog.setTitle("Enum " + enumType.toString());

    for (Object literal : enumType.literals) {
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append(String.valueOf(literal));
      addParagraph(asb.build());
    }
  }


    void renderClass(InstanceType classifier) {
    alertDialog.setTitle("Class " + classifier.toString());


    for (PropertyDescriptor descriptor : classifier.getPropertyDescriptors()) {
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      addPropertyLink(asb, descriptor);
      addParagraph(asb.build());
    }

  }


  private void addParagraph(AnnotatedString annotatedString) {
    TextView textView = new TextView(mainActivity);
    textView.setText(AnnotatedStringConverter.toSpanned(mainActivity, annotatedString, AnnotatedStringConverter.NO_LINKED_LINE));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
    linearLayout.addView(textView);
  }


  interface Predicate<T> {
    boolean test(T value);
  }
}
