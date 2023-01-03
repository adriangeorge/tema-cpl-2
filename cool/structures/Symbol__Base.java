package cool.structures;

public class Symbol__Base {
    public String name;

    public Symbol__Base(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
