package de.bokeh.skred.red;

import java.math.BigInteger;

public abstract class Data extends ValueNode {
    
    abstract public int getTag();
    
    @Override
    public String toString(boolean parens, int maxDepth) {
        if (getTag() == 1 && isProperList()) {
            if (isPrintableString()) {
                return toStringRep();
            }
            return toListString(maxDepth - 1);
        }
        StringBuilder b = new StringBuilder("#");
        b.append(getTag());
        b.append('{');
        String sep = "";
        int n = getNumFields();
        for (int i = 0; i < n; i++) {
            b.append(sep);
            sep = ", ";
            b.append(getField(i));
        }
        b.append('}');
        return b.toString();
    }

    private String toStringRep() {
        StringBuilder b = new StringBuilder();
        b.append('"');
        Node d = this;
        while (d.getTag() != 0) {
            char c = (char) d.getField(0).intValue().intValue();
            if (c == '\\' || c == '"') {
                b.append('\\');
            }
            b.append(c);
            d = d.getField(1);
        }
        b.append('"');
        return b.toString();
    }

    private boolean isPrintableString() {
        if (getTag() == 0 && getNumFields() == 0) {
            return true;
        }
        if (getTag() == 1 && getNumFields() == 2) {
            Node v = getField(0);
            if (!isPrintableChar(v)) {
                return false;
            }
            Node t = getField(1);
            return (t instanceof Data) && ((Data) t).isProperList();
        }
        return false;
    }

    private static boolean isPrintableChar(Node v) {
        if (!(v instanceof Int)) {
            return false;
        }
        BigInteger m = v.intValue();
        int n = m.intValue();
        return (n >= 32 && n <= 126 && m.bitLength() < 8);
    }

    private String toListString(int depth) {
        StringBuilder b = new StringBuilder();
        b.append('[');
        Data d = this;
        String sep = "";
        while (d.getTag() != 0) {
            b.append(sep);
            sep = ", ";
            b.append(d.getField(0).toString(false, depth));
            d = (Data) d.getField(1);
        }
        b.append(']');
        return b.toString();
    }

    private boolean isProperList() {
        if (getTag() == 0 && getNumFields() == 0) {
            return true;
        }
        if (getTag() == 1 && getNumFields() == 2) {
            Node t = getField(1);
            return (t instanceof Data) && ((Data) t).isProperList();
        }
        return false;
    }

    @Override
    public boolean hasVars() {
        return false;
    }

    // Factory part ------------------------------------------

    public static Data valueOf(int tag) {
        switch (tag) {
        case 0: return Enum0;
        case 1: return Enum1;
        case 2: return Enum2;
        case 3: return Enum3;
        case 4: return Enum4;
        default: return new Data0(tag);
        }
    }
    
    public static Data valueOf(int tag, Node f0) {
        return new DataN(tag, f0);
    }
    
    public static Data valueOf(int tag, Node f0, Node f1) {
        if (tag == 1)
            return new Data1_2(f0, f1);
        return new Data2(tag, f0, f1);
    }
    
    public static Data valueOf(int tag, Node[] fields) {
        switch (fields.length) {
        case 0: return valueOf(tag);
        case 2: return valueOf(tag, fields[0], fields[1]);
        default: return new DataN(tag, fields);
        }
    }
    
    private static final Data Enum0 = new Data0_0();
    private static final Data Enum1 = new Data0(1);
    private static final Data Enum2 = new Data0(2);
    private static final Data Enum3 = new Data0(3);
    private static final Data Enum4 = new Data0(4);
    
    public static Data makeString(String s) {
        Data r = valueOf(0);
        for (int i = s.length() - 1; i >= 0; i--) {
            int c = s.charAt(i);
            r = valueOf(1, Int.valueOf(c), r);
        }
        return r;
    }

}
