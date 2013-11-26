package de.bokeh.skred.red;

public class Data0_0 extends Data {

    @Override
    public int getNumFields() {
        return 0;
    }

    @Override
    public Node getField(int i) {
        throw new FieldIndexOutOfBoundsException(i, this);
    }

    @Override
    public int getTag() {
        return 0;
    }

}
