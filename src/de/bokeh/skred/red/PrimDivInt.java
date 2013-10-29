package de.bokeh.skred.red;

/**
 * Int div
 */
class PrimDivInt extends Function {

    public PrimDivInt() {
        super("/", 2);
    }

    @Override
    Node exec(RedContext c) {
        c.rearrange2();
        c.eval();
        c.swap();
        c.eval();
        Node a2 = c.getTos();
        Node a1 = c.get1();
        int n1 = a1.intValue();
        int n2 = a2.intValue();
        int r = n1 / n2;
        Node result = Int.valueOf(r);
        c.pop2();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
