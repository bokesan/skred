package de.bokeh.skred.red;

/**
 * The B' combinator.
 * <p>
 * Reduction rule:
 * B' c f g x ==&gt; c f (g x)
 */
class CombB1 extends Function {

    public CombB1() {
        super("B'", 4);
    }

    @Override
    Node exec(RedContext c) {
        Node a_c = c.getArg1();
        Node a_f = c.getArg2();
        Node a_g = c.getArg3();
        Node a_x = c.getArg(4);
        Node redex = c.get(4);
        Node g1 = c.mkApp(a_c, a_f);
        Node g2 = c.mkApp(a_g, a_x);
        redex.overwriteApp(g1, g2);
        c.set3(g1);
        c.pop3();
        return null;
    }

}
