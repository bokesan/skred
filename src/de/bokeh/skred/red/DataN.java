package de.bokeh.skred.red;

public class DataN extends Data {

    private final int tag;
    private final Node[] fields;
    
    public DataN(int tag, Node... fields) {
        this.tag = tag;
        this.fields = new Node[fields.length];
        System.arraycopy(fields, 0, this.fields, 0, fields.length);
    }

    @Override
    public Node getField(int i) {
        if (i < 0 || i >= fields.length)
            throw new FieldIndexOutOfBoundsException(i, this);
        return fields[i];
    }

    @Override
    public int getNumFields() {
        return fields.length;
    }

    @Override
    public int getTag() {
        return tag;
    }

}
