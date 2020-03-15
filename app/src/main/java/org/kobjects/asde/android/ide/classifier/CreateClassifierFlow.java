package org.kobjects.asde.android.ide.classifier;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.android.ide.symbol.PropertyNameValidator;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Struct;
import org.kobjects.asde.lang.classifier.Trait;

public class CreateClassifierFlow {

  public enum Kind {
    CLASS, TRAIT
  }

  public static void start(MainActivity mainActivity, Kind kind) {
    new InputFlowBuilder(mainActivity, kind == Kind.CLASS ? "New Class" : "New Trait")
        .addInput("Name", null, new PropertyNameValidator(mainActivity.program.mainModule))
        .setPositiveLabel("Create")
        .start(result -> {
          Classifier clasifier = kind == Kind.CLASS ? new Struct(mainActivity.program) : new Trait(mainActivity.program);
          mainActivity.program.setDeclaration(result[0], clasifier);
          mainActivity.program.notifyProgramChanged();
        });
  }

}
