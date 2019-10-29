package org.kobjects.asde.android.ide.text;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.TextValidator;
import org.kobjects.asde.lang.Format;

public class ExpressionValidator extends TextValidator {
  private MainActivity mainActivity;

  public ExpressionValidator(MainActivity mainActivity) {
    this.mainActivity = mainActivity;

  }

  @Override
  public String validate(String text) {
    if (text.trim().isEmpty()) {
      return "Expression must not be empty.";
    }
    try {
      mainActivity.program.parser.parseExpression(text);
    } catch (Exception e) {
      return Format.exceptionToString(e);
    }
    return null;
  }
}


