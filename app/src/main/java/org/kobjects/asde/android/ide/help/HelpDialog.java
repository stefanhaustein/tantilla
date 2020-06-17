package org.kobjects.asde.android.ide.help;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.kobjects.asde.android.ide.text.TextRenderer;
import org.kobjects.markdown.AnnotatedString;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.markdown.Section;
import org.kobjects.markdown.Text;
import org.kobjects.asde.android.ide.Dimensions;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.AnnotatedStringConverter;
import org.kobjects.asde.lang.help.HelpGenerator;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.classifier.Property;

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

  public HelpDialog navigateTo(Object o) {
    navigationStack.add(o);
    updateContent();
    return this;
  }

  HelpDialog updateContent() {
    linearLayout.removeAllViews();
    Object o = navigationStack.get(navigationStack.size() - 1);

    Text helpText = HelpGenerator.renderHelp(mainActivity.program, o);

    alertDialog.setTitle(helpText.title);
    TextRenderer.render(mainActivity, linearLayout, helpText, this);

    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(navigationStack.size() > 1);
    return this;
  }

}
