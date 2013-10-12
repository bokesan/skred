package de.bokeh.skred.red;

/**
 * The K' combinator.
 * <p>
 * Reduction rule:
 * K' x c ==&gt; c
 */
class CombK1 extends Function {

    public CombK1() {
        super("K'", 2);
    }

    @Override
    Node exec(RedContext c) {
        Node a_c = c.getArg2();
        c.pop2();
        c.getTos().overwriteInd(a_c);
        c.setTos(a_c);
        return null;
    }

}
