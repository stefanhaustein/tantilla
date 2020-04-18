package org.kobjects.asde.android.ide.errors;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.AnnotatedStringConverter;
import org.kobjects.asde.lang.io.Format;

public class Errors {
  public static void show(MainActivity mainActivity, Exception exception) {
    show(mainActivity, exception, AnnotatedStringConverter.NO_LINKED_LINE);
  }

  public static void show(MainActivity mainActivity, Exception exception, int linkedLine) {
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mainActivity);
    builder.setTitle("Error");
    builder.setMessage(Format.exceptionToString(exception));
    builder.setNegativeButton("Dismiss", null);
    if (linkedLine > AnnotatedStringConverter.NO_LINKED_LINE) {
      builder.setPositiveButton("Edit", (dialog, index) ->
          mainActivity.console.edit(linkedLine));
    }
    builder.show();
  }

}
