package de.bokeh.skred.red;

/**
 * The K' combinator.
 * <p>
 * Reduction rule:
 * K' x c ==&gt; c
 */
class CombK1_Eval extends Function {

    public CombK1_Eval() {
        super("K'", 2);
    }

    @Override
    Node exec(RedContext c) {
        Node a_c = c.getArg2();
        c.set1(a_c);
        c.pop1();
        c.get1().overwriteHole();
        c.eval();
        Node result = c.getTos();
        c.get1().overwriteInd(result);
        c.set1(result);
        c.pop1();
        return null;
    }

}
