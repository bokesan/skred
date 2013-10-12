package de.bokeh.skred.red;

import java.io.*;

public class PrimStdPort extends Function {

    public PrimStdPort() {
        super("_STD_PORT", 1);
    }

    @Override
    Node exec(RedContext c) {
        // Argument intentionally not evaluated
        Node a = c.getArg(1);
        assert a.intValue() == 0;
        Node result = new InputPort(new InputStreamReader(System.in));
        c.pop1();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
