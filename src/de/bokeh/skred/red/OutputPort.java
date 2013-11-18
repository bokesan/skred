package de.bokeh.skred.red;

import java.io.PrintStream;

public class OutputPort extends ValueNode {

    private final PrintStream out;
    
    public OutputPort(PrintStream out) {
        this.out = out;
    }
    
    public PrintStream getPrintStream() {
        return out;
    }

    @Override
    public String toString(boolean parens, int maxDepth) {
        return "OutputPort[" + out + "]";
    }

}
