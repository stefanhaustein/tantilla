package org.kobjects.asde.android.ide.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import com.google.android.material.textfield.TextInputLayout;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.TextValidator;

import java.util.ArrayList;

public class InputFlowBuilder {
  private final MainActivity mainActivity;
  private final String title;

  private String message;
  private String positiveLabel = "Ok";
  private String negativeLabel = "Cancel";
  private ArrayList<Input> inputList = new ArrayList<>();
  private String confirmationCheckbox;


  public InputFlowBuilder(MainActivity mainActivity, String title) {
    this.mainActivity = mainActivity;
    this.title = title;
  }

  public InputFlowBuilder setMessage(String message) {
    this.message = message;
    return this;
  }

  public InputFlowBuilder addInput(String label, Object initialValue, TextValidator validator) {
    inputList.add(new Input(label, initialValue, validator));
    return this;
  }

  public InputFlowBuilder setConfirmationCheckbox(String message) {
    this.confirmationCheckbox = message;
    return this;
  }

  public InputFlowBuilder setPositiveLabel(String label) {
    this.positiveLabel = label;
    return this;
  }


  public void start(ResultHandler resultHandler) {
    AlertDialog[] alert = new AlertDialog[1];
    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
    alertBuilder.setTitle(title);
    if (message != null) {
      alertBuilder.setMessage(message);
    }

    ArrayList<TextInputLayout> inputLayoutList = new ArrayList<>();

    TextWatcher overWatch = new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable editable) {
        boolean anyError = false;
        for (int i = 0; i < inputList.size(); i++) {
          if (inputList.get(i).validator != null) {
            TextInputLayout inputLayout = inputLayoutList.get(i);
            String error = inputList.get(i).validator.validate(inputLayout.getEditText().getText().toString());
            inputLayout.setError(error);
            anyError |= error != null;
          }
        }
        alert[0].getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!anyError);
      }
    };

    if (!inputList.isEmpty()) {
      LinearLayout mainLayout = new LinearLayout(alertBuilder.getContext());

      mainLayout.setOrientation(LinearLayout.VERTICAL);

      if (message == null) {
        mainLayout.addView(new TextView(mainActivity));
      }

      for (Input input : inputList) {
        TextInputLayout inputLayout = new TextInputLayout(mainActivity);
        if (input.label != null) {
          inputLayout.setHint(input.label);
          inputLayout.setHintEnabled(true);
        }
        inputLayoutList.add(inputLayout);
        EditText editText = new EditText(mainActivity);
        inputLayout.addView(editText);
        if (input.initialValue != null) {
          inputLayout.getEditText().setText(String.valueOf(input.initialValue));
          if (input.initialValue instanceof Integer) {
            inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
          }
        }
        inputLayout.setErrorEnabled(true);

        if (input.validator != null) {
          inputLayout.getEditText().addTextChangedListener(overWatch);
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 56;
        layoutParams.rightMargin = 56;
        mainLayout.addView(inputLayout, layoutParams);
      }

      alertBuilder.setView(mainLayout);
    }

    if (confirmationCheckbox != null) {
      alertBuilder.setMultiChoiceItems(new CharSequence[]{confirmationCheckbox}, new boolean[1], new DialogInterface.OnMultiChoiceClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
          alert[0].getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(b);
        }
      });
    }

    alertBuilder.setNegativeButton(negativeLabel, null);
    if (resultHandler != null) {
      alertBuilder.setPositiveButton(positiveLabel, (a, b) -> {
        String[] result = new String[inputLayoutList.size()];
        for (int i = 0; i < inputLayoutList.size(); i++) {
          result[i] = inputLayoutList.get(i).getEditText().getText().toString();
        }
        resultHandler.handleResult(result);
      });
    }

    alert[0] = alertBuilder.show();

    overWatch.afterTextChanged(null);

    if (confirmationCheckbox != null) {
      alert[0].getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    }
  }

  static class Input {
    final String label;
    final Object initialValue;
    final TextValidator validator;

    Input(String label, Object initialValue, TextValidator validator) {
      this.label = label;
      this.initialValue = initialValue;
      this.validator = validator;
    }
  }


  public interface ResultHandler {
    void handleResult(String... result);
  }

}
