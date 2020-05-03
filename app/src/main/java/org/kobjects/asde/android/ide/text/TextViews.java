package org.kobjects.asde.android.ide.text;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiTextView;

public class TextViews {


  public static void adjustMovementMethod(EmojiTextView textView) {
    CharSequence text = textView.getText();
    System.out.println("Text: " + text);
    if (text instanceof Spannable) {
      Spannable spannable = (Spannable) text;
      if (spannable.getSpans(0, Integer.MAX_VALUE, ClickableSpan.class).length > 0) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
      } else {
        textView.setMovementMethod(null);
      }
    }
  }
}
