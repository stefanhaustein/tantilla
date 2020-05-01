package org.kobjects.asde.android.ide.text;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.widget.TextView;

public class TextViews {


  public static void adjustMovementMethod(TextView textView) {
    CharSequence text = textView.getText();
    if (text instanceof Spannable) {
      Spannable spannable = (Spannable) text;
      if (spannable.getSpans(0, spannable.length(), ClickableSpan.class).length > 0) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
      } else {
        textView.setMovementMethod(null);
      }
    }
  }
}
