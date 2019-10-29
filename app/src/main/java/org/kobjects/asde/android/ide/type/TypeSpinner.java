package org.kobjects.asde.android.ide.type;

import androidx.appcompat.widget.AppCompatSpinner;
import android.widget.ArrayAdapter;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;

public class TypeSpinner extends AppCompatSpinner {

    final ArrayList<Type> typeList = new ArrayList<>();
    final ArrayList<String> labelList = new ArrayList<>();

    void addType(Type type) {
        typeList.add(type);
        labelList.add(type.toString());
    }

    public TypeSpinner(MainActivity mainActivity) {
        this(mainActivity, null);
    }

    public TypeSpinner(MainActivity mainActivity, String voidLabel) {
        super(mainActivity);

        if (voidLabel != null) {
            typeList.add(Types.VOID);
            labelList.add(voidLabel);
        }

        addType(Types.BOOLEAN);
        addType(Types.NUMBER);
        addType(Types.STRING);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item, labelList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setAdapter(adapter);
        setSelection(0);
    }

    public void selectType(Type type) {
        if (type != null) {
            setSelection(typeList.indexOf(type));
        }
    }

    public Type getSelectedType() {
        return typeList.get(getSelectedItemPosition());
    }
}
