package org.kobjects.asde.android.ide;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.Annotations;
import org.kobjects.annotatedtext.Span;
import org.kobjects.asde.lang.Format;

public class AnnotatedStringConverter {


  public static Spanned toSpanned(MainActivity mainActivity, AnnotatedString annotated, boolean linked) {

      SpannableString s = new SpannableString(annotated.toString());
      for (final Span span : annotated.spans()) {
        if (span.annotation == Annotations.ACCENT_COLOR) {
          s.setSpan(new ForegroundColorSpan(Colors.ACCENT), span.start, span.end, 0);
        } else if (span.annotation instanceof Exception) {
          s.setSpan(new BackgroundColorSpan(Colors.ORANGE), span.start, span.end, 0);
          if (linked) {
            ((Exception) span.annotation).printStackTrace();
            s.setSpan(new ClickableSpan() {
              @Override
              public void onClick(View widget) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mainActivity);
                builder.setTitle("Error");
                builder.setMessage(Format.exceptionToString((Exception) span.annotation));
                builder.show();
              }
            }, span.start, span.end, 0);
          }
        }
      }
      return s;
    }

  }

