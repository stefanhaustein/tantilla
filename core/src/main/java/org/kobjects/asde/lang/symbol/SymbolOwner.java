package org.kobjects.asde.lang.symbol;

public interface SymbolOwner {
    StaticSymbol getSymbol(String name);
    void removeSymbol(StaticSymbol symbol);
    void addSymbol(StaticSymbol symbol);
}
