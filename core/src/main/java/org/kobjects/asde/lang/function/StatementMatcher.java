package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.CodeLine;

public interface StatementMatcher {
    boolean statementMatches(CodeLine line, int index, Node statement);
}
