package de.bokeh.skred.red;

public class PrimPred extends Function {

    public PrimPred() {
        super("pred", 1);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg(1));
        c.eval();
        Node x = c.getTos();
        Int r = Int.valueOf(x.intValue() - 1);
        c.pop1();
        c.getTos().overwriteInd(r);
        c.setTos(r);
        return r;
    }

}
