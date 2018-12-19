package org.kobjects.asde.android.ide.editor;

import android.support.v7.widget.AppCompatSpinner;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;

public class TypeSpinner extends AppCompatSpinner {

    final ArrayList<Type> typeList = new ArrayList<>();

    public TypeSpinner(MainActivity mainActivity) {
        super(mainActivity);

        typeList.add(Types.BOOLEAN);
        typeList.add(Types.NUMBER);
        typeList.add(Types.STRING);

        ArrayAdapter<Type> adapter = new ArrayAdapter<Type>(mainActivity, android.R.layout.simple_spinner_item, typeList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setAdapter(adapter);
        setSelection(1);
    }

    public void selectType(Type type) {
        if (type != null) {
            setSelection(typeList.indexOf(type));
        }
    }

    public Type getSelectedType() {
        return (Type) getSelectedItem();
    }
}
