package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Statement;
import org.kobjects.asde.lang.type.FunctionType;

import java.util.List;
import java.util.TreeMap;

public class CallableUnit {
    FunctionType type;
    public TreeMap<Integer, List<Statement>> code = new TreeMap<>();
}
