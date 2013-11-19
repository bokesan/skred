package de.bokeh.skred.red;

import java.io.*;

public class PrimStdPort extends Function {

    public PrimStdPort() {
        super("primStdPort", 1);
    }

    @Override
    Node exec(RedContext c) {
        // Argument intentionally not evaluated
        Node a = c.getArg(1);
        Node result;
        switch (a.intValue().intValue()) {
        case 0:
            result = new InputPort(new InputStreamReader(System.in));
            break;
        case 1:
            result = new OutputPort(System.out);
            break;
        case 2:
            result = new OutputPort(System.err);
            break;
        default:
            throw new AssertionError("primStdPort: invalid argument: " + a);
        }
        
        c.pop1();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
