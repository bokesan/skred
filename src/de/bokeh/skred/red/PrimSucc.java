package de.bokeh.skred.red;

import java.math.BigInteger;

public class PrimSucc extends Function {

    public PrimSucc() {
        super("succ", 1);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg(1));
        c.eval();
        Node x = c.getTos();
        Int r = Int.valueOf(x.intValue().add(BigInteger.ONE));
        c.pop1();
        c.getTos().overwriteInd(r);
        c.setTos(r);
        return r;
    }

}
