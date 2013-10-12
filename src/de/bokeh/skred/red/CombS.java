package de.bokeh.skred.red;

/**
 * The S combinator.
 * <p>
 * Reduction rule:
 * S f g x ==&gt; f x (g x)
 */
class CombS extends Function {

    public CombS() {
        super("S", 3);
    }

    @Override
    Node exec(RedContext c) {
        Node a_f = c.getArg1();
        Node a_g = c.getArg2();
        Node a_x = c.getArg3();
        Node redex = c.get3();
        Node g1 = c.mkApp(a_f, a_x);
        Node g2 = c.mkApp(a_g, a_x);
        redex.overwriteApp(g1, g2);
        c.set2(g1);
        c.pop2();
        return null;
    }

}
