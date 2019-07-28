package org.kobjects.asde.android.ide.editor;

import android.support.design.widget.TextInputLayout;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.TextValidator;
import org.kobjects.asde.lang.Format;

public class ExpressionValidator extends TextValidator {
  private MainActivity mainActivity;

  ExpressionValidator(MainActivity mainActivity, TextInputLayout textInputLayout) {
    super(textInputLayout);
    this.mainActivity = mainActivity;

  }

  @Override
  public String validate(String text) {
    if (text.isEmpty()) {
      return "Name must not be empty.";
    }
    try {
      mainActivity.program.parser.parseExpression(text);
    } catch (Exception e) {
      return Format.exceptionToString(e);
    }
    return null;
  }
}


