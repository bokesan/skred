package de.bokeh.skred.red;

public abstract class Data extends ValueNode {

    private final int tag;
    
    public Data(int tag) {
        assert tag >= 0;
        this.tag = tag;
    }
    
    public int getTag() {
        return tag;
    }
    
    abstract public int getNumFields();
    abstract public Node getField(int i);
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("#");
        b.append(tag);
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
        return new Data2(tag, f0, f1);
    }
    
    public static Data valueOf(int tag, Node[] fields) {
        return new DataN(tag, fields);
    }
    
    private static final Data Enum0 = new Data0(0);
    private static final Data Enum1 = new Data0(1);
    private static final Data Enum2 = new Data0(2);
    private static final Data Enum3 = new Data0(3);
    private static final Data Enum4 = new Data0(4);

}
