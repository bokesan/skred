package de.bokeh.skred.red;

public final class Dbl extends ValueNode {

    private final double val;
    
    private Dbl(double val) {
        this.val = val;
    }

    public static Dbl valueOf(double val) {
        return new Dbl(val);
    }
    
    public double doubleValue() {
        return val;
    }
    
    public String toString(int maxDepth) {
        return Double.toString(val);
    }
    
}
