package de.bokeh.skred.red;

/**
 * Relop greater
 */
class PrimGreater extends Function {

    public PrimGreater() {
        super("gt", 2);
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
        boolean r = n1 > n2;
        Node result = Data.valueOf(r?1:0);
        c.pop2();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
