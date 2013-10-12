package de.bokeh.skred.red;

/**
 * The K combinator.
 * <p>
 * Reduction rule:
 * K c x ==&gt; c
 */
class CombK extends Function {

    public CombK() {
        super("K", 2);
    }

    @Override
    Node exec(RedContext c) {
        Node a_c = c.getArg1();
        c.pop2();
        c.getTos().overwriteInd(a_c);
        c.setTos(a_c);
        return null;
    }

}
