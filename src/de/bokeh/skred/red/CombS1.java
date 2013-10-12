package de.bokeh.skred.red;

/**
 * The S' combinator.
 * <p>
 * Reduction rule:
 * S' c f g x ==&gt; c (f x) (g x)
 */
class CombS1 extends Function {

    public CombS1() {
        super("S'", 4);
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
        Node g3 = c.mkApp(a_g, a_x);
        redex.overwriteApp(g2, g3);
        c.set3(g2);
        c.pop3();
        return null;
    }

}
