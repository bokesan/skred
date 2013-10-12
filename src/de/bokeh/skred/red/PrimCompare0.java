package de.bokeh.skred.red;

public class PrimCompare0 extends Function {

    public PrimCompare0() {
        super("compare0", 4);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg(4));
        c.eval();
        Node x = c.getTos();
        int n = x.intValue();
        int arg = (n <= 0) ? ((n == 0) ? 2 : 1) : 3;
        x = c.getArg(arg);
        c.pop(4);
        c.getTos().overwriteInd(x);
        c.setTos(x);
        return null;
    }

}
