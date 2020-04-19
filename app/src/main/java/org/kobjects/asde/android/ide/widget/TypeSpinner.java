package org.kobjects.asde.android.ide.widget;

import androidx.appcompat.widget.AppCompatSpinner;
import android.widget.ArrayAdapter;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.type.Type;

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

        addType(Types.BOOL);
        addType(Types.FLOAT);
        addType(Types.STR);

        for (Property property : mainActivity.program.mainModule.getProperties()) {
            if (!property.isMutable() && property.getStaticValue() instanceof Type) {
                addType((Type) property.getStaticValue());
            }
        }

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
