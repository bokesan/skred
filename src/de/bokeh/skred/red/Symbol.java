package de.bokeh.skred.red;

public class Symbol extends ValueNode {

    private final String sym;
    
    private Symbol(String s) {
        sym = s;
    }
    
    public static Symbol valueOf(String s) {
        return new Symbol(s);
    }

    @Override
    public String toString(boolean parens, int d) {
        return sym;
    }

    @Override
    public boolean hasVars() {
        return true;
    }

}
