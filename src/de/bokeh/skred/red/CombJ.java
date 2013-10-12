package de.bokeh.skred.red;

/**
 * The J combinator.
 * <p>
 * Reduction rule:
 * J f g x ==&gt; f g
 */
class CombJ extends Function {

    public CombJ() {
        super("J", 3);
    }

    @Override
    Node exec(RedContext c) {
        Node a_f = c.getArg1();
        Node a_g = c.getArg2();
        Node redex = c.get3();
        redex.overwriteApp(a_f, a_g);
        c.set2(a_f);
        c.pop2();
        return null;
    }

}
