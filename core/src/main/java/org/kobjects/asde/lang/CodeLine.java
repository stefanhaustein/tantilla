package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Statement;

import java.util.ArrayList;
import java.util.List;

public class CodeLine {
    public ArrayList<Node> statements;
    public int indent;

    public CodeLine(List<? extends Node> statements) {
        this.statements = new ArrayList<>(statements);
    }


    public void toString(AnnotatedStringBuilder sb) {
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        for (int i = 0; i < statements.size(); i++) {
            if (i > 0) {
                sb.append(i == 0 || (statements.get(i - 1) instanceof Statement &&  ((Statement) statements.get(i - 1)).kind == Statement.Kind.IF) ? " " : " : ");
            }
            sb.append(statements.get(i).toString());
        }
    }

    @Override
    public String toString() {
        AnnotatedStringBuilder sb = new AnnotatedStringBuilder();
        toString(sb);
        return sb.toString();
    }
}
