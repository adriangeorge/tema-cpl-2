package cool.structures;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Symbol_Method extends Symbol__Base implements Scope__Base {
    protected Symbol_Type parentScope;
    protected Map<String, Symbol__Base> symbols = new LinkedHashMap<>();
    public String return_type;

    public Symbol_Method(String name, String return_type, Symbol_Type parentScope) {
        super(name);
        this.parentScope = parentScope;
        this.return_type = return_type;
    }

    public ArrayList<Symbol_Identifier> getFormals() {
        ArrayList<Symbol_Identifier> formal_list = new ArrayList<>();
        for (var f : symbols.values()) {
            formal_list.add((Symbol_Identifier) f);
        }
        return formal_list;
    }

    public Symbol_Type getParentScope() {
        return parentScope;
    }

    public String getReturn_type() {
        return return_type;
    }

    public void setReturn_type(String return_type) {
        this.return_type = return_type;
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
    public Symbol__Base lookup_attribute(String str) {
        var sym = symbols.get(str);

        if (sym != null)
            return sym;

        if (parentScope != null)
            return parentScope.lookup_attribute(str);

        return null;
    }

    @Override
    public Symbol__Base lookup_method(String str) {
        var sym = symbols.get(str);

        if (sym != null)
            return sym;

        if (parentScope != null)
            return parentScope.lookup_method(str);

        return null;
    }

}