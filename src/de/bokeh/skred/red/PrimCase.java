package de.bokeh.skred.red;

public class PrimCase extends Function {

    private final int numAlts;
    private final boolean hasDefault;
    
    private PrimCase(int numAlts, boolean d) {
        super("case" + numAlts + (d ? "d" : ""), numAlts + (d ? 2 : 1));
        this.numAlts = numAlts;
        this.hasDefault = d;
    }

    private static final PrimCase case2 = new PrimCase(2, false);
    
    public static Function of(int numAlts) {
        if (numAlts == 2) {
            return case2;
        }
        return new PrimCase(numAlts, false);
    }

    public static Function withDefault(int numAlts) {
        return new PrimCase(numAlts, true);
    }
    
    @Override
    Node exec(RedContext c) {
        int arity = hasDefault ? (numAlts + 2) : (numAlts + 1);
        
        Node redex = c.get(arity);
        c.setTos(redex.getArg());
        c.eval();
        Node x = c.getTos();
        int tag = x.getTag();
        if (tag < numAlts) {
            Node alt = c.getArg(tag + 1);
            redex.overwriteApp(alt, x);
            c.pop(arity);
            return null;
        }
        if (hasDefault) {
            Node alt = c.getArg(arity - 1);
            redex.overwriteInd(alt);
            c.pop(arity);
            c.setTos(alt);
            return null;
        }
        throw new RedException("case: unhandled tag " + tag);
    }

}
