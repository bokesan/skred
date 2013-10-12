package de.bokeh.skred.red;

/**
 * The W combinator.
 * <p>
 * Reduction rule:
 * W f x ==&gt; f x x
 */
class CombW extends Function {

    public CombW() {
        super("W", 2);
    }

    @Override
    Node exec(RedContext c) {
        Node a_f = c.getArg1();
        Node a_x = c.getArg2();
        Node redex = c.get2();
        Node g1 = c.mkApp(a_f, a_x);
        redex.overwriteApp(g1, a_x);
        c.set1(g1);
        c.pop1();
        return null;
    }

}
