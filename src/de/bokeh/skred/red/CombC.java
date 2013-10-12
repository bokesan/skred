package de.bokeh.skred.red;

/**
 * The C combinator.
 * <p>
 * Reduction rule:
 * C f x y ==&gt; f y x
 */
class CombC extends Function {

    public CombC() {
        super("C", 3);
    }

    @Override
    Node exec(RedContext c) {
        Node a_f = c.getArg1();
        Node a_x = c.getArg2();
        Node a_y = c.getArg3();
        Node redex = c.get3();
        Node g1 = c.mkApp(a_f, a_y);
        redex.overwriteApp(g1, a_x);
        c.set2(g1);
        c.pop2();
        return null;
    }

}
