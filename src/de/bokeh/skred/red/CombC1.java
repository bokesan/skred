package de.bokeh.skred.red;

/**
 * The C' combinator.
 * <p>
 * Reduction rule:
 * C' c f g x ==&gt; c (f x) g
 */
class CombC1 extends Function {

    public CombC1() {
        super("C'", 4);
    }

    @Override
    Node exec(RedContext c) {
        Node a_c = c.getArg1();
        Node a_f = c.getArg2();
        Node a_g = c.getArg3();
        Node a_x = c.getArg(4);
        Node redex = c.get(4);
        Node g1 = c.mkApp(a_f, a_x);
        Node g2 = c.mkApp(a_c, g1);
        redex.overwriteApp(g2, a_g);
        c.set3(g2);
        c.pop3();
        return null;
    }

}
