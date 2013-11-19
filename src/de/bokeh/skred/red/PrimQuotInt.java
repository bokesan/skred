package de.bokeh.skred.red;

import java.math.BigInteger;

/**
 * Integer division
 */
class PrimQuotInt extends Function {

    public PrimQuotInt() {
        super("quot", 2);
    }

    @Override
    Node exec(RedContext c) {
        c.rearrange2();
        c.eval();
        c.swap();
        c.eval();
        Node a2 = c.getTos();
        Node a1 = c.get1();
        BigInteger r = a1.intValue().divide(a2.intValue());
        Node result = Int.valueOf(r);
        c.pop2();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
