package de.bokeh.skred.red;

public class Data1_2 extends Data {

    private final Node f0;
    private final Node f1;
    
    public Data1_2(Node f0, Node f1) {
        this.f0 = f0;
        this.f1 = f1;
    }

    @Override
    public Node getField(int i) {
        switch (i) {
        case 0: return f0;
        case 1: return f1;
        default: throw new FieldIndexOutOfBoundsException(i, this);
        }
    }

    @Override
    public int getNumFields() {
        return 2;
    }

    @Override
    public int getTag() {
        return 1;
    }

}
