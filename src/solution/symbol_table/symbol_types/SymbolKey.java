package solution.symbol_table.symbol_types;

import java.util.Objects;

public class SymbolKey {

    public String name;
    public SymbolKeyType type;

    public SymbolKey(String name, SymbolKeyType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SymbolKey symbolKey = (SymbolKey) o;
        return Objects.equals(name, symbolKey.name) &&
                type == symbolKey.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }



}
