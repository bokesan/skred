package de.bokeh.skred.red;


public class PrimCase extends Function {

    private final int[] arities;
    private final boolean hasDefault;
    
    private PrimCase(int[] arities, boolean d) {
        super(getName(arities, d), arities.length + (d ? 2 : 1));
        this.arities = arities;
        this.hasDefault = d;
    }
    
    private static final String getName(int[] arities, boolean d) {
        StringBuilder b = new StringBuilder();
        b.append("case{");
        b.append(arities[0]);
        for (int i = 1; i < arities.length; i++) {
            b.append(',');
            b.append(arities[i]);
        }
        if (d) {
            b.append(",d");
        }
        b.append('}');
        return b.toString();
    }
    
    private static final PrimCase CASE_BOOL  = new PrimCase(new int[]{0,0}, false);
    private static final PrimCase CASE_MAYBE = new PrimCase(new int[]{0,1}, false);
    private static final PrimCase CASE_LIST  = new PrimCase(new int[]{0,2}, false);
    
    public static Function of(int[] arities) {
        if (arities.length == 2 && arities[0] == 0) {
            switch (arities[1]) {
            case 0: return CASE_BOOL;
            case 1: return CASE_MAYBE;
            case 2: return CASE_LIST;
            }
        }
        return new PrimCase(arities, false);
    }

    public static Function withDefault(int[] arities) {
        return new PrimCase(arities, true);
    }
    
    @Override
    Node exec(RedContext c) {
        int arity = getArity();
        
        Node redex = c.get(arity);
        c.setTos(redex.getArg());
        c.eval();
        Node x = c.getTos();
        int tag = x.getTag();
        if (tag < arities.length) {
            Node alt = c.getArg(tag + 1);
            int mf = arities[tag];
            switch (mf) {
            case 0:
                // TODO: evalprojections
                redex.overwriteInd(alt);
                c.pop(arity);
                c.setTos(alt);
                return null;
            case 1:
                break;
            case 2:
                alt = c.mkApp(alt, x.getField(0));
                break;
            default:
                for (int i = 0; i < mf - 1; i++) {
                    alt = c.mkApp(alt, x.getField(i));
                }
                break;
            }
            redex.overwriteApp(alt, x.getField(mf - 1));
            c.pop(arity - 1);
            c.setTos(alt);
            return null;
        }
        if (hasDefault) {
            Node alt = c.getArg(arity - 1);
            redex.overwriteApp(alt, x);
            c.pop(arity);
            return null;
        }
        throw new RedException("case: unhandled tag " + tag);
    }

}
