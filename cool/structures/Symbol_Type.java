package cool.structures;

import java.util.*;

public class Symbol_Type extends Symbol__Base implements Scope__Base {
    // Definitions for scope
    protected Scope__Base parentScope;
    protected Map<String, Symbol__Base> attribute_namespace = new LinkedHashMap<>();
    protected Map<String, Symbol__Base> method_namespace = new LinkedHashMap<>();
    protected Map<String, Symbol__Base> selected_namespace;

    public String inheritance;

    public Symbol_Type(String name, String inheritance, Scope__Base parentScope) {
        super(name);
        this.inheritance = inheritance;
        this.parentScope = parentScope;
    }

    public String getInheritance() {
        return inheritance;
    }

    public void setInheritance(String inheritance) {
        this.inheritance = inheritance;
    }

    public String getName() {
        return name;
    }

    public void setName(String class_name) {
        this.name = class_name;
    }

    public boolean add_method(Symbol__Base sym) {
        selected_namespace = method_namespace;
        return add(sym);
    }

    public boolean add_attribute(Symbol__Base sym) {
        selected_namespace = attribute_namespace;
        return add(sym);
    }

    @Override
    public boolean add(Symbol__Base sym) {
        if (selected_namespace.containsKey(sym.getName()))
            return false;

        selected_namespace.put(sym.getName(), sym);

        return true;
    }

    public Symbol_Method lookup_method(String str) {
        selected_namespace = method_namespace;
        var sym = selected_namespace.get(str);

        if (sym != null)
            return (Symbol_Method)sym;

        if (parentScope != null)
            return (Symbol_Method) parentScope.lookup_method(str);

        return null;
    }

    public Symbol__Base lookup_attribute(String str) {
        selected_namespace = attribute_namespace;
        var sym = selected_namespace.get(str);

        if (sym != null)
            return sym;

        if (parentScope != null)
            return parentScope.lookup_attribute(str);

        return null;
    }

    @Override
    public Scope__Base getParentScope() {
        return parentScope;
    }

    // Define base types
    public static final Symbol_Type INT = new Symbol_Type("Int", null, SymbolTable.globals);
    public static final Symbol_Type BOOL = new Symbol_Type("Bool", null, SymbolTable.globals);
    public static final Symbol_Type STRING = new Symbol_Type("String", null, SymbolTable.globals);

    public static final Symbol_Type Object = new Symbol_Type("Object", null, SymbolTable.globals);

    public static final Symbol_Type IO = new Symbol_Type("IO", null, SymbolTable.globals);

    public static final Symbol_Type SELF_TYPE = new Symbol_Type("SELF_TYPE", null, SymbolTable.globals);
}