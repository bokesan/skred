package de.bokeh.skred.red;

public class PrimZero extends Function {

    public PrimZero() {
        super("zero", 1);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg(1));
        c.eval();
        int val = c.getTos().intValue();
        c.pop1();
        Node r = Data.valueOf(val == 0 ? 1 : 0);
        c.getTos().overwriteInd(r);
        c.setTos(r);
        return r;
    }

}
