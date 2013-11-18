package de.bokeh.skred.red;

import java.io.*;

public class InputPort extends ValueNode {

    private Reader in;
    
    public InputPort(Reader in) {
        this.in = in;
    }
    
    public int read() {
        try {
            return in.read();
        } catch (IOException ex) {
            throw new RedException("IO-Error", ex);
        }
    }
    
    public String toString(boolean parens, int d) {
        return "InputPort[" + in + "]";
    }
    
}
