package org.kobjects.asde.android.ide.editor;

import android.app.AlertDialog;

import org.kobjects.asde.android.ide.MainActivity;

public class DeleteFlow {
    private final MainActivity mainActivity;
    private final String name;

    public DeleteFlow(MainActivity mainActivity, String name) {
        this.mainActivity = mainActivity;
        this.name = name;
    }

    public void start() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
        alertBuilder.setTitle("Confirm Delete");
        alertBuilder.setMessage("Delete symbol '" + name + "'?");
        alertBuilder.setNegativeButton("Cancel", null);
        alertBuilder.setPositiveButton("Delete", (a,b) -> {
           mainActivity.program.deleteSymbol(name);
           mainActivity.sync(true);
        });

        alertBuilder.show();
    }


}
