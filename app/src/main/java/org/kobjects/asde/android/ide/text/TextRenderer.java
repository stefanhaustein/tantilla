package org.kobjects.asde.android.ide.text;

import android.content.Context;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup;
import android.widget.TextView;

import org.kobjects.asde.android.ide.Dimensions;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.help.HelpDialog;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.markdown.AnnotatedString;
import org.kobjects.markdown.Section;
import org.kobjects.markdown.Text;

public class TextRenderer {

  public static void render(MainActivity mainActivity, ViewGroup target, Text text, HelpDialog helpDialog) {

    for (Section section : text.sections) {
      switch (section.kind) {
        case SUBTITLE:
          addSubtitle(target, section.text);
          break;
        default:
          addParagraph(mainActivity, target, section.text, helpDialog);
          break;
      }
    }
  }


    private static void addSubtitle(ViewGroup target, CharSequence text) {
      Context context = target.getContext();
      TextView textView = new TextView(context);
      SpannableString spanned = new SpannableString(text);
      //spanned.setSpan(new StyleSpan(BOLD), 0, text.length(), 0);
      textView.setText(spanned);
      int padding = Dimensions.dpToPx(context, 12);
      textView.setPadding(0, (target.getChildCount() == 0) ? 0 : padding, 0, padding);
      target.addView(textView);
    }


    private static void addParagraph(MainActivity mainActivity, ViewGroup target, CharSequence charSequence, HelpDialog helpDialog) {
      if (charSequence != null) {
        TextView textView = new TextView(mainActivity);
        textView.setText(AnnotatedStringConverter.toSpanned(mainActivity, AnnotatedString.of(charSequence), helpDialog));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        target.addView(textView);
      }
    }
}
