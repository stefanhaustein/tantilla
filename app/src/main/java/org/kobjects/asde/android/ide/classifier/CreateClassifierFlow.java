package org.kobjects.asde.android.ide.classifier;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.android.ide.property.PropertyNameValidator;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.ClassType;
import org.kobjects.asde.lang.classifier.GenericProperty;
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
          Classifier clasifier = kind == Kind.CLASS ? new ClassType(mainActivity.program) : new Trait(mainActivity.program);
          synchronized (mainActivity.program) {
            mainActivity.program.mainModule.putProperty(GenericProperty.createStatic(mainActivity.program.mainModule, result[0], clasifier));
          }
          mainActivity.program.notifyProgramChanged();
        });
  }

}
