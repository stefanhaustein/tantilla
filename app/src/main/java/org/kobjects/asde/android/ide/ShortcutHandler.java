package org.kobjects.asde.android.ide;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.kobjects.asde.R;

public class ShortcutHandler {

    MainActivity mainActivity;
    LinearLayout shortcutLayout;
    EditText nameInput;
    Bitmap bitmap;

    ShortcutHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        String programName = mainActivity.program.reference.name;

        shortcutLayout = new LinearLayout(mainActivity);
        shortcutLayout.setOrientation(LinearLayout.VERTICAL);

        TextView nameLabel = new TextView(mainActivity);
        nameLabel.setText("Name");
        shortcutLayout.addView(nameLabel);
        nameInput = new EditText(mainActivity);
        nameInput.setText(programName);
        shortcutLayout.addView(nameInput);
    }

    void run() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);

        if (ShortcutManagerCompat.isRequestPinShortcutSupported(mainActivity)) {
            alert.setTitle("Home Screen Shortcut");

            if (shortcutLayout.getParent() != null) {
                ((ViewGroup)shortcutLayout.getParent()).removeView(shortcutLayout);
            }

            if (bitmap != null) {
                alert.setIcon(new BitmapDrawable(bitmap));
            }
            alert.setView(shortcutLayout);

            alert.setNegativeButton("Cancel", null);

            alert.setNeutralButton("Icon", (a, b) -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                mainActivity.startActivityForResult(intent, MainActivity.PICK_SHORTCUT_ICON_REQUEST_CODE);
            });

            alert.setPositiveButton("Add", (a, b) -> {
                ShortcutInfoCompat.Builder shortcutBuilder = new ShortcutInfoCompat.Builder(mainActivity, Integer.toHexString((int) (Math.random() * 0x7fffffff)));
                shortcutBuilder.setIntent(new Intent(mainActivity, MainActivity.class).setAction(Intent.ACTION_MAIN).putExtra("run", mainActivity.program.reference.toString()));
                shortcutBuilder.setShortLabel(nameInput.getText().toString());
                if (bitmap != null) {
                    shortcutBuilder.setIcon(IconCompat.createWithBitmap(bitmap));
                }
                ShortcutManagerCompat.requestPinShortcut(mainActivity, shortcutBuilder.build(), null);
            });
        } else {
            alert.setTitle("Error");
            alert.setMessage("Shortcut is not supported.");
        }
        alert.show();

    }


}
