package de.bokeh.skred.red;

/**
 * The I combinator.
 * <p>
 * Reduction rule:
 * I x ==&gt; x
 */
class CombI_Eval extends Function {

    public CombI_Eval() {
        super("I", 1);
    }

    @Override
    Node exec(RedContext c) {
        Node a_x = c.getArg1();
        c.setTos(a_x);
        c.get1().overwriteHole();
        c.eval();
        Node result = c.getTos();
        c.get1().overwriteInd(result);
        c.set1(result);
        c.pop1();
        return null;
    }

}
