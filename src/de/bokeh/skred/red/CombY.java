package de.bokeh.skred.red;

/**
 * The Y combinator.
 * <p>
 * Reduction rule:
 * Y f ==&gt; f (Y f)
 */
class CombY extends Function {

    public CombY() {
        super("Y", 1);
    }

    @Override
    Node exec(RedContext c) {
        Node redex = c.get(1);
        Node a_f = redex.getArg();
        redex.overwriteApp(a_f, redex);
        c.setTos(a_f);
        return null;
    }

}
