package de.bokeh.skred.red;


public final class Int extends ValueNode {

    private final int value;
    
    private static final Int[] V = new Int[130];
    static {
        for (int i = -2; i < 128; i++)
            V[i+2] = new Int(i);
    }

    private Int(int n) {
	value = n;
    }
    
    public static Int valueOf(int n) {
        if (n >= -2 && n <= 127)
            return V[n+2];
        return new Int(n);
    }

    public int intValue() {
	return value;
    }

    public String toString(int d) {
	return Integer.toString(value);
    }

}
