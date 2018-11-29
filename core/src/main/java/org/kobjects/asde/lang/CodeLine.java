package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.IfStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CodeLine {
    public ArrayList<Node> statements;
    public int indent;

    public CodeLine(List<? extends Node> statements) {
        this.statements = new ArrayList<>(statements);
    }


    public void toString(AnnotatedStringBuilder sb, Map<Node, Exception> errors) {
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        for (int i = 0; i < statements.size(); i++) {
            if (i > 0) {
                sb.append((i == 0 || statements.get(i - 1) instanceof IfStatement) ? "" : " : ");
            }
            statements.get(i).toString(sb, errors);
        }
    }

    @Override
    public String toString() {
        AnnotatedStringBuilder sb = new AnnotatedStringBuilder();
        toString(sb, Collections.<Node, Exception>emptyMap());
        return sb.toString();
    }
}
