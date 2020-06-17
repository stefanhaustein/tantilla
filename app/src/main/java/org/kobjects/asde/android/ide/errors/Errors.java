package org.kobjects.asde.android.ide.errors;

import org.kobjects.markdown.AnnotatedString;
import org.kobjects.markdown.Span;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.AnnotatedStringConverter;
import org.kobjects.asde.lang.exceptions.ExceptionWithReplacementPropolsal;
import org.kobjects.asde.lang.io.Format;

public class Errors {
  public static void show(MainActivity mainActivity, Exception exception) {
    show(mainActivity, exception, AnnotatedStringConverter.NO_LINKED_LINE);
  }

  public static void show(MainActivity mainActivity, Exception exception, int linkedLine) {
    show(mainActivity, exception, null, null, linkedLine);
  }

  public static void show(MainActivity mainActivity, AnnotatedString annotatedString, Span span, int linkedLine) {
    show(mainActivity, (Exception) span.annotation, annotatedString, span, linkedLine);
  }


  private static void show(MainActivity mainActivity, Exception exception, AnnotatedString annotatedString, Span span, int linkedLine) {
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mainActivity);

    if (exception instanceof ExceptionWithReplacementPropolsal && annotatedString != null && exception != null) {
      builder.setTitle(Format.exceptionToString(exception));

      CharSequence spanned = annotatedString.subSequence(span.start, span.end);

      String[] proposals = ((ExceptionWithReplacementPropolsal) exception).replacementProposals;

      for (int i = 0; i < proposals.length; i++) {
        proposals[i] = String.format(proposals[i], spanned);
      }

      int[] checkedItem = new int[1];
      builder.setSingleChoiceItems(proposals, 0, (dialog, which) -> {
        checkedItem[0] = which;
      });
      builder.setNeutralButton("Replace", (a, b) -> {
        String replacement = spanned.subSequence(0, span.start) + proposals[checkedItem[0]] + spanned.subSequence(span.end, spanned.length());
        mainActivity.controlView.codeEditText.setText(replacement);
      });
    } else {
      builder.setTitle("Error");
      builder.setMessage(Format.exceptionToString(exception));
    }


    builder.setNegativeButton("Dismiss", null);
    if (linkedLine > AnnotatedStringConverter.NO_LINKED_LINE) {
      builder.setPositiveButton("Edit", (dialog, index) ->
          mainActivity.console.edit(linkedLine));
    }
    builder.show();
  }

}
