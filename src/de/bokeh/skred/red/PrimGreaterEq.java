package de.bokeh.skred.red;

/**
 * Relop gr_eq
 */
class PrimGreaterEq extends Function {

    public PrimGreaterEq() {
        super("ge", 2);
    }

    @Override
    Node exec(RedContext c) {
        c.rearrange2();
        c.eval();
        c.swap();
        c.eval();
        Node a2 = c.getTos();
        Node a1 = c.get1();
        boolean r = a1.intValue().compareTo(a2.intValue()) >= 0;
        Node result = Data.valueOf(r?1:0);
        c.pop2();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
