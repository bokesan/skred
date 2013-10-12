package de.bokeh.skred.red;

public class RedException extends RuntimeException {

    static final long serialVersionUID = 0L;
    
    public RedException(String message) {
        super(message);
    }

    public RedException(String message, Throwable cause) {
        super(message, cause);
    }

}
