package org.kobjects.asde.android.ide.text;

import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.Annotations;
import org.kobjects.annotatedtext.Span;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.help.HelpDialog;
import org.kobjects.asde.lang.Format;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.node.SymbolNode;

public class AnnotatedStringConverter {

  public static final int NO_LINKS = -1;
  public static final int NO_LINKED_LINE = 0;


  public static SpannableString toSpanned(MainActivity mainActivity, AnnotatedString annotated, int linkedLine) {

      SpannableString s = new SpannableString(annotated.toString());
      for (final Span span : annotated.spans()) {
        if (span.annotation == Annotations.ACCENT_COLOR) {
          s.setSpan(new ForegroundColorSpan(Colors.ACCENT), span.start, span.end, 0);
        } else if (span.annotation instanceof Exception) {
          s.setSpan(new BackgroundColorSpan(Colors.RED), span.start, span.end, 0);
          if (linkedLine > NO_LINKS) {
            ((Exception) span.annotation).printStackTrace();
            s.setSpan(new ClickableSpan() {
              @Override
              public void onClick(View widget) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mainActivity);
                builder.setTitle("Error");
                Exception exception = (Exception) span.annotation;
                builder.setMessage(Format.exceptionToString(exception));
                builder.setNegativeButton("Dismiss", null);
                if (linkedLine > NO_LINKED_LINE) {
                  builder.setPositiveButton("Edit", (dialog, index) ->
                      mainActivity.console.edit(linkedLine));
                }
                builder.show();
              }
            }, span.start, span.end, 0);
          }
        } else if (span.annotation instanceof StaticSymbol && linkedLine > NO_LINKS) {
          s.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
              HelpDialog.showHelp(mainActivity, (StaticSymbol) span.annotation);
            }
          }, span.start, span.end, 0);
        } else if (span.annotation instanceof Runnable && linkedLine > NO_LINKS) {
          s.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
              ((Runnable) span.annotation).run();
            }
          }, span.start, span.end, 0);
        }
      }
      return s;
    }

  }

