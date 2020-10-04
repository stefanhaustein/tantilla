package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.expression.Node;

public interface StatementMatcher {
    boolean statementMatches(CodeLine line, int index, Node statement);
}
