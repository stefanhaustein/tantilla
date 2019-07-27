package org.kobjects.asde.android.ide.editor;

import android.app.AlertDialog;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.StaticSymbol;

public class DeleteFlow {
    private final MainActivity mainActivity;
    private final StaticSymbol symbol;

    public DeleteFlow(MainActivity mainActivity, StaticSymbol symbol) {
        this.mainActivity = mainActivity;
        this.symbol = symbol;
    }

    public void start() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
        alertBuilder.setTitle("Confirm Delete");
        alertBuilder.setMessage("Delete symbol '" + symbol.getName() + "'?");
        alertBuilder.setNegativeButton("Cancel", null);
        alertBuilder.setPositiveButton("Delete", (a,b) -> {
           symbol.getOwner().removeSymbol(symbol);
        });

        alertBuilder.show();
    }


}
