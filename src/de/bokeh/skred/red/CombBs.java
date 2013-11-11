package de.bokeh.skred.red;

/**
 * The B* combinator.
 * <p>
 * Reduction rule:
 * B* c f g x ==&gt; c (f (g x))
 */
class CombBs extends Function {

    public CombBs() {
        super("Bs", 4);
    }

    @Override
    Node exec(RedContext c) {
        Node a_c = c.getArg1();
        Node a_f = c.getArg2();
        Node a_g = c.getArg3();
        Node a_x = c.getArg(4);
        Node redex = c.get(4);
        Node g1 = c.mkApp(a_g, a_x);
        Node g2 = c.mkApp(a_f, g1);
        redex.overwriteApp(a_c, g2);
        c.set3(a_c);
        c.pop3();
        return null;
    }

}
