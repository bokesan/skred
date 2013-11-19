package de.bokeh.skred.red;

/**
 * Relop eq
 */
class PrimEq extends Function {

    public PrimEq() {
        super("eq", 2);
    }

    @Override
    Node exec(RedContext c) {
        c.rearrange2();
        c.eval();
        c.swap();
        c.eval();
        Node a2 = c.getTos();
        Node a1 = c.get1();
        boolean r = a1.intValue().equals(a2.intValue());
        Node result = Data.valueOf(r?1:0);
        c.pop2();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
