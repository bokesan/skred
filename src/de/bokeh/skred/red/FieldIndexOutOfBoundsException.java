package de.bokeh.skred.red;

public class FieldIndexOutOfBoundsException extends RedException {

    static final long serialVersionUID = 0L;
    
    public FieldIndexOutOfBoundsException(int index, Data node) {
        super("field " + index + " does not exist: " + node);
    }

}
