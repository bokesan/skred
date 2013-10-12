package de.bokeh.skred.red;

/**
 * The B combinator.
 * <p>
 * Reduction rule:
 * B f g x ==&gt; f (g x)
 */
class CombB extends Function {

    public CombB() {
        super("B", 3);
    }

    @Override
    Node exec(RedContext c) {
        Node a_f = c.getArg1();
        Node a_g = c.getArg2();
        Node a_x = c.getArg3();
        Node redex = c.get3();
        Node g1 = c.mkApp(a_g, a_x);
        redex.overwriteApp(a_f, g1);
        c.set2(a_f);
        c.pop2();
        return null;
    }

}
