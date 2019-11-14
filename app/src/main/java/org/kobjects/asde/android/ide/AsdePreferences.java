package org.kobjects.asde.android.ide;

import android.content.Context;
import android.content.SharedPreferences;

import org.kobjects.asde.lang.io.ProgramReference;

import java.util.ArrayList;
import java.util.List;

public class AsdePreferences {

  private static final String HELLO_COPIED = "hello-copied";
  private static final String RECENT = "recent";
  private static final int MAX_RECENTS = 9;

  private final SharedPreferences sharedPreferences;
  private final MainActivity mainActivity;

  public AsdePreferences(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
    sharedPreferences = mainActivity.getPreferences(Context.MODE_PRIVATE);
  }

  public ProgramReference getProgramReference() {
    return ProgramReference.parse(sharedPreferences.getString(RECENT + 0,
        "\n" + mainActivity.console.nameToReference(null).url + "\ntrue"));
  }

  public boolean getHelloCopied() {
    return sharedPreferences.getBoolean(HELLO_COPIED, false);
  }

  public void setHelloCopied(boolean newValue) {
    sharedPreferences.edit().putBoolean(HELLO_COPIED, newValue).commit();
  }

  public void setProgramReference(ProgramReference programReference) {
    List<ProgramReference> list = getRecents();
    list.remove(programReference);
    list.add(0, programReference);
    while(list.size() > MAX_RECENTS) {
      list.remove(list.size() - 1);
    }
    SharedPreferences.Editor editor = sharedPreferences.edit();
    for (int i = 0; i < list.size(); i++) {
      editor.putString(RECENT + i, list.get(i).toString());
    }
    editor.putString(RECENT+ list.size(), "");
    editor.commit();
  }


  public List<ProgramReference> getRecents() {
    ArrayList result = new ArrayList();
    for (int i = 0; i < 10; i++) {
      String recent = sharedPreferences.getString(RECENT + i, null);
      if (recent == null || recent.isEmpty()) {
        break;
      }
      ProgramReference reference = ProgramReference.parse(recent);
      if (!reference.name.isEmpty()) {
        result.add(ProgramReference.parse(recent));
      }
    }
    return result;
  }

}
