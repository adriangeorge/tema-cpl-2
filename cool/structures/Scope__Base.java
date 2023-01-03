package cool.structures;

public interface Scope__Base {

    public boolean add(Symbol__Base sym);

    public Symbol__Base lookup_method(String str);
    public Symbol__Base lookup_attribute(String str);

    public Scope__Base getParentScope();
}
