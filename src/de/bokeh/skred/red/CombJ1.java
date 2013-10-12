package de.bokeh.skred.red;

/**
 * The J' combinator.
 * <p>
 * Reduction rule:
 * J' k f g x ==&gt; k f g
 */
class CombJ1 extends Function {

    public CombJ1() {
        super("J'", 4);
    }

    @Override
    Node exec(RedContext c) {
        Node a_k = c.getArg1();
        Node a_f = c.getArg2();
        Node a_g = c.getArg3();
        Node redex = c.get(4);
        Node g1 = c.mkApp(a_k, a_f);
        redex.overwriteApp(g1, a_g);
        c.set3(g1);
        c.pop3();
        return null;
    }

}
