package org.kobjects.asde.android.ide;

import android.content.Context;
import android.content.SharedPreferences;

import org.kobjects.asde.lang.ProgramReference;

public class AsdePreferences {

    static final String PROGRAM_REFERENCE = "program_reference";
    static final String THEME = "theme";

    private final SharedPreferences sharedPreferences;
    private final MainActivity mainActivity;

    public AsdePreferences(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        sharedPreferences = mainActivity.getPreferences(Context.MODE_PRIVATE);
    }

    public ProgramReference getProgramReference() {
        return ProgramReference.parse(sharedPreferences.getString(PROGRAM_REFERENCE,
                "Unnamed\n" + mainActivity.nameToReference("Unnamed") + "\nfalse"));
    }

    public void setProgramReference(ProgramReference programReference) {
        sharedPreferences.edit().putString(PROGRAM_REFERENCE, programReference.toString()).commit();
    }

    public Colors.Theme getTheme() {
        try {
            return Colors.Theme.valueOf(sharedPreferences.getString(THEME, Colors.Theme.LIGHT.name()));
        } catch (IllegalArgumentException e) {
            return Colors.Theme.LIGHT;
        }
    }

    public void setTheme(Colors.Theme theme) {
        sharedPreferences.edit().putString(THEME, theme.name()).commit();
    }

}
