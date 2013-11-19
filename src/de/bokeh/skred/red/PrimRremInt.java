package de.bokeh.skred.red;

import java.math.BigInteger;

/**
 * Int Rrem
 */
class PrimRremInt extends Function {

    public PrimRremInt() {
        super("Rrem", 2);
    }

    @Override
    Node exec(RedContext c) {
        c.rearrange2();
        c.eval();
        c.swap();
        c.eval();
        Node a2 = c.getTos();
        Node a1 = c.get1();
        BigInteger r = a2.intValue().remainder(a1.intValue());
        Node result = Int.valueOf(r);
        c.pop2();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
