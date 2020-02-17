package org.kobjects.asde.android.ide.help;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.android.ide.Dimensions;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.AnnotatedStringConverter;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class HelpDialog {

  public static void showHelp(MainActivity mainActivity, List<Object> stack) {
    new HelpDialog(mainActivity, stack);
  }

  public static void showHelp(MainActivity mainActivity, Object o) {
    showHelp(mainActivity, Collections.singletonList(o));
  }


    public static void showHelp(MainActivity mainActivity ) {
    showHelp(mainActivity, Collections.singletonList(null));
  }



  final MainActivity mainActivity;
  final LinearLayout linearLayout;
  final AlertDialog alertDialog;
  final ArrayList<Object> navigationStack = new ArrayList<>();


  HelpDialog(MainActivity mainActivity, List<Object> stack) {
    this.mainActivity = mainActivity;
    navigationStack.addAll(stack);
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
    alertBuilder.setPositiveButton("Close", null);
      alertBuilder.setNegativeButton("Back", (a, b) -> {
        showHelp(mainActivity, navigationStack.subList(0, navigationStack.size() - 1));
    });

    alertDialog = alertBuilder.create();
    alertDialog.show();

    updateContent();
  }

  void appendLink(AnnotatedStringBuilder asb, Object linked) {
    if (linked instanceof StaticSymbol) {
      StaticSymbol symbol = (StaticSymbol) linked;
      if (symbol.getValue() instanceof Function) {
        appendFunctionLink(asb, (StaticSymbol) linked);
      } else if (symbol.getValue() instanceof Type) {
        appendLink(asb, symbol.getValue());
      } else {
        asb.append(symbol.getName());
        asb.append(": ");
        appendLink(asb, symbol.getType());
      }
    } else if (linked instanceof Property) {
      appendPropertyLink(asb, (Property) linked);
    } else {
      appendLink(asb, String.valueOf(linked), linked);
    }
  }

  private void appendMethodLink(AnnotatedStringBuilder asb, Property property) {
    appendLink(asb, property.getName(), property);
    appendShortSignature(asb, (FunctionType) property.getType());
  }

  private void appendPropertyLink(AnnotatedStringBuilder asb, Property property) {
    if (isMethod(property)) {
      appendMethodLink(asb, property);
    } else {
      appendLink(asb, property.getName(), property);
      asb.append(": ");
      asb.append(String.valueOf(property.getType()));
    }

  }

  void appendLink(AnnotatedStringBuilder asb, String text, Object linked) {
    asb.append(text, linked);
  }

  void addSignaure(String name, FunctionType functionType) {

    addSubtitle("Parameter");

    for (int i = 0; i < functionType.getParameterCount(); i++) {
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append(String.valueOf(i + 1)).append(") ");
      appendLink(asb, functionType.getParameterType(i));
      addParagraph(asb.build());
    }

    if (functionType.getReturnType() != Types.VOID) {
      addSubtitle("Return Type");

      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append("  ");
      appendLink(asb, functionType.getReturnType());
      addParagraph(asb.build());
    }
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

  void appendFunctionLink(AnnotatedStringBuilder sb, StaticSymbol functionSymbol) {
    Function function = (Function) functionSymbol.getValue();

    appendLink(sb, functionSymbol.getName(), functionSymbol);

    appendShortSignature(sb, function.getType());
  }


  public HelpDialog navigateTo(Object o) {
    navigationStack.add(o);
    updateContent();
    return this;
  }


  void addAll(String subtitle, Predicate<StaticSymbol> filter) {
    addSubtitle(subtitle);

    for (StaticSymbol s : mainActivity.program.getSymbols()) {
      if (s.getScope() == GlobalSymbol.Scope.BUILTIN && filter.test(s)) {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        appendLink(asb, s);
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

      addAll("Classes", s -> s.getValue() instanceof Classifier);

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
    SpannableString spanned = new SpannableString(text);
    //spanned.setSpan(new StyleSpan(BOLD), 0, text.length(), 0);
    textView.setText(spanned);
    int padding = Dimensions.dpToPx(mainActivity, 12);
    textView.setPadding(0, (linearLayout.getChildCount() == 0) ? 0 : padding, 0, padding);
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
    } else if (o instanceof Classifier) {
      renderClass((Classifier) o);
    } else if (o instanceof Property) {
      renderProperty((Property) o);
    } else if (o instanceof EnumType) {
      renderEnum((EnumType) o);
    } else {
      alertDialog.setTitle("Unknown");
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append(String.valueOf(o));
      addParagraph(asb.build());
    }
  }

  private void renderProperty(Property o) {
    if (isMethod(o)) {
      renderMethod(o);
      return;
    }

    alertDialog.setTitle("Property " + o.getName());

    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    asb.append("Type: ");
    appendLink(asb, o.getType());

    addParagraph(asb.build());
  }

  private void renderMethod(Property o) {
    alertDialog.setTitle("Method " + o.getName());
    addSignaure(o.getName(), (FunctionType) o.getType());
  }


  void renderFunction(StaticSymbol functionSymbol) {
    Function function = (Function) functionSymbol.getValue();

    alertDialog.setTitle("Function " + functionSymbol.getName());

    addSignaure(functionSymbol.getName(), function.getType());

    CharSequence documentation = function.getDocumentation();
    if (documentation != null) {
      addSubtitle("Description");
      addParagraph(documentation);
    }
  }


  void renderEnum(EnumType enumType) {
    alertDialog.setTitle("Enum " + enumType.toString());

    addSubtitle("Literals");

    for (Object literal : enumType.literals) {
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append(String.valueOf(literal));
      addParagraph(asb.build());
    }
  }


    void renderClass(Classifier classifier) {
    alertDialog.setTitle("Class " + classifier.toString());

    addParagraph(AnnotatedString.of(classifier.getDocumentation()));

    for (boolean methods : new boolean[]{false, true}) {

      boolean first = true;

      for (Property descriptor : classifier.getAllProperties()) {
        if (isMethod(descriptor) == methods) {
          if (first) {
            addSubtitle(methods ? "Methods" : "Properties");
            first = false;
          }
          AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
          appendPropertyLink(asb, descriptor);
          addParagraph(asb.build());
        }
      }
    }
  }

  private static  boolean isMethod(Property descriptor) {
    return descriptor.getType() instanceof FunctionType && !(descriptor.getType() instanceof ListType);
  }


  private void addParagraph(CharSequence charSequence) {
    if (charSequence != null) {
      TextView textView = new TextView(mainActivity);
      textView.setText(AnnotatedStringConverter.toSpanned(mainActivity, AnnotatedString.of(charSequence), this));
      textView.setMovementMethod(LinkMovementMethod.getInstance());
      linearLayout.addView(textView);
    }
  }


  interface Predicate<T> {
    boolean test(T value);
  }
}
