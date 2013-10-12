package de.bokeh.skred.red;

/**
 * The I combinator.
 * <p>
 * Reduction rule:
 * I x ==&gt; x
 */
class CombI extends Function {

    public CombI() {
        super("I", 1);
    }

    @Override
    Node exec(RedContext c) {
        Node a_x = c.getArg1();
        c.pop1();
        c.getTos().overwriteInd(a_x);
        c.setTos(a_x);
        return null;
    }

}
