package cool.structures;

import java.util.*;

public class Scope_Default implements Scope__Base {

    private Map<String, Symbol__Base> symbols = new LinkedHashMap<>();
    private Scope__Base parent;

    public Scope_Default(Scope__Base parent) {
        this.parent = parent;
    }

    public boolean add(Symbol__Base sym) {
        // Reject duplicates in the same scope.
        if (symbols.containsKey(sym.getName()))
            return false;

        symbols.put(sym.getName(), sym);

        return true;
    }

    public void add_replace(Symbol__Base sym) {
        symbols.put(sym.getName(), sym);
    }

    @Override
    public String toString() {
        return symbols.values().toString();
    }

    @Override
    public Scope__Base getParentScope() {
        return parent;
    }

    @Override
    public Symbol__Base lookup_method(String str) {
        var sym = symbols.get(str);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookup_method(str);

        return null;
    }

    @Override
    public Symbol__Base lookup_attribute(String str) {
        var sym = symbols.get(str);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookup_attribute(str);

        return null;
    }

}
