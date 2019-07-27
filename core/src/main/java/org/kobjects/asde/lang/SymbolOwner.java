package org.kobjects.asde.lang;

public interface SymbolOwner {
    StaticSymbol getSymbol(String name);
    void removeSymbol(StaticSymbol symbol);
}
