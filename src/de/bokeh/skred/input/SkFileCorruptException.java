package de.bokeh.skred.input;

import java.io.IOException;

public class SkFileCorruptException extends IOException {

    static final long serialVersionUID = 0L;
    
    public SkFileCorruptException(String message) {
        super(message);
    }

    public SkFileCorruptException(String message, Throwable cause) {
        super(message, cause);
    }

}
