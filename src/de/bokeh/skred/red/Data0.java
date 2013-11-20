package de.bokeh.skred.red;

public class Data0 extends Data {

    Data0(int tag) {
        super(tag);
    }

    @Override
    public int getNumFields() {
        return 0;
    }

    @Override
    public Node getField(int i) {
        throw new FieldIndexOutOfBoundsException(i, this);
    }

}
