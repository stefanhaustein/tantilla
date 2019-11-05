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
import org.kobjects.asde.lang.type.Function;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.MetaType;
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


  HelpDialog navigateTo(Object o) {
    navigationStack.add(o);
    updateContent();
    return this;
  }

  HelpDialog updateContent() {
    linearLayout.removeAllViews();
    Object o = navigationStack.get(navigationStack.size() - 1);
    if (o == null) {
      alertDialog.setTitle("Help");
      linearLayout.removeAllViews();

      subtitle("Constants");

      for (StaticSymbol s : mainActivity.program.getSymbols()) {
        if (s.getScope() == GlobalSymbol.Scope.BUILTIN && !(s.getValue() instanceof Function) && !(s.getType() instanceof MetaType)) {
          addSymbolDescription(s, false);
        }
      }

      subtitle("Types");

      for (StaticSymbol s : mainActivity.program.getSymbols()) {
        if (s.getScope() == GlobalSymbol.Scope.BUILTIN && s.getType() instanceof MetaType) {
          addSymbolDescription(s, false);
        }
      }

      subtitle("Functions");

      for (StaticSymbol s : mainActivity.program.getSymbols()) {
        if (s.getScope() == GlobalSymbol.Scope.BUILTIN && s.getValue() instanceof Function) {
          addFunctionDescription(s, false);
        }
      }


    } else if (o instanceof StaticSymbol) {
      alertDialog.setTitle(((StaticSymbol) o).getName());
      addSymbolDescription((StaticSymbol) o, true);
    } else if (o instanceof Type) {
      alertDialog.setTitle(o.toString());
      addTypeDescription((Type) o, true);
    }

    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(navigationStack.size() > 1);
    return this;
  }

  private void subtitle(String text) {
    TextView textView = new TextView(mainActivity);
    textView.setText(text);
    textView.setPadding(0, 50, 0, 50);
    linearLayout.addView(textView);
  }

  void addLink(AnnotatedStringBuilder asb, String text, Object linked) {
    asb.append(text, (Runnable) () -> navigateTo(linked));
  }

  void addTypeDescription(Type type, boolean full) {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    asb.append("Type: ");
    asb.append(type.toString());
    addText(asb.build());
  }

  void addFunctionDescription(StaticSymbol functionSymbol, boolean full) {

      Function function = (Function) functionSymbol.getValue();

      AnnotatedStringBuilder sb = new AnnotatedStringBuilder();
      if (full) {
        sb.append(functionSymbol.getName());
      } else {
        addLink(sb, functionSymbol.getName(), functionSymbol);
      }
      sb.append("(");

      FunctionType functionType = function.getType();
      for (int i = 0; i < functionType.getParameterCount(); i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(functionType.getParameterType(i).toString());
      }
      sb.append(")");

      if (functionType.getReturnType() != Types.VOID) {
        sb.append(" -> ").append(functionType.getReturnType().toString());
      }

      addText(sb.build());
    }

    void addDescription(Object o, boolean full) {
      if (o instanceof StaticSymbol) {
        addSymbolDescription((StaticSymbol) o, full);
      } else if (o instanceof Type) {
        addTypeDescription((Type) o, full);
      }
    }

    void addSymbolDescription(StaticSymbol symbol, boolean full) {
      if (symbol.getValue() instanceof Function) {
        addFunctionDescription(symbol, full);
      } else if (symbol.getValue() instanceof Type) {
        addTypeDescription((Type) symbol.getValue(), full);
      } else {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        asb.append(symbol.getName());
        asb.append(": ");
        addLink(asb, symbol.getType().toString(), symbol.getType());

        addText(asb.build());
      }
    }

  private void addText(AnnotatedString annotatedString) {
    TextView textView = new TextView(mainActivity);
    textView.setText(AnnotatedStringConverter.toSpanned(mainActivity, annotatedString, AnnotatedStringConverter.NO_LINKED_LINE));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
    linearLayout.addView(textView);
  }


}
