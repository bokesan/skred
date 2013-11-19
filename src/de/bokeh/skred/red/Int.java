package de.bokeh.skred.red;

import java.math.BigInteger;


public final class Int extends ValueNode {

    private final BigInteger value;
    
    private static final Int[] V = new Int[130];
    static {
        for (int i = -2; i < 128; i++)
            V[i+2] = new Int(i);
    }

    private Int(int n) {
	value = BigInteger.valueOf(n);
    }
    
    private Int(BigInteger n) {
        value = n;
    }
    
    public static Int valueOf(int n) {
        if (n >= -2 && n <= 127)
            return V[n+2];
        return new Int(n);
    }
    
    public static Int valueOf(BigInteger val) {
        int n = val.intValue();
        if (n >= -2 && n <= 127 && val.bitLength() < 8) {
            return V[n+2];
        }
        return new Int(val);
    }

    public BigInteger intValue() {
	return value;
    }

    public String toString(boolean parens, int d) {
        return value.toString();
    }

}
