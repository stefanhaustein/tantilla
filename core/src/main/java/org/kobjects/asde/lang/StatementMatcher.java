package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.CodeLine;

public interface StatementMatcher {
    boolean statementMatches(CodeLine line, int index, Node statement);
}
