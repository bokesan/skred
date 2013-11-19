package de.bokeh.skred.red;

import java.math.BigInteger;

/**
 * Int Rsub
 */
class PrimRsubInt extends Function {

    public PrimRsubInt() {
        super("Rsub", 2);
    }

    @Override
    Node exec(RedContext c) {
        c.rearrange2();
        c.eval();
        c.swap();
        c.eval();
        Node a2 = c.getTos();
        Node a1 = c.get1();
        BigInteger r = a2.intValue().subtract(a1.intValue());
        Node result = Int.valueOf(r);
        c.pop2();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
