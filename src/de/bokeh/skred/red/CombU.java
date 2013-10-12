package de.bokeh.skred.red;

public class CombU extends Function {

    public CombU() {
        super("U", 2);
    }

    @Override
    Node exec(RedContext c) {
        Node r = c.get(2);
        Node x = r.getArg();
        Node c0 = c.mkApp(Function.getLISTCASE(),
                          c.mkApp(Function.ERROR, Int.valueOf(0)));
        Node c1 = c.mkApp(c.mkApp(c0, Function.getK()), x);
        Node c2 = c.mkApp(c.mkApp(c0, Function.getK1()), x);
        Node f = c.getArg(1);
        Node c3 = c.mkApp(f, c1);
        c.set(1, c3);
        c.setTos(f);
        r.overwriteApp(c3, c2);
        return null;
    }

}
