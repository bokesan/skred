package de.bokeh.skred.red;

public class PrimCompare0_Eval extends Function {

    public PrimCompare0_Eval() {
        super("compare0", 4);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg(4));
        c.eval();
        Node x = c.getTos();
        int n = x.intValue();
        int arg = (n <= 0) ? ((n == 0) ? 2 : 1) : 3;
        c.set(3, c.getArg(arg));
        c.get(4).overwriteHole();
        c.pop3();
        c.eval();
        x = c.getTos();
        c.pop1();
        c.getTos().overwriteInd(x);
        c.setTos(x);
        return null;
    }

}
