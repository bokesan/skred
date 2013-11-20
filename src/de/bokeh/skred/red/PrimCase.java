package de.bokeh.skred.red;


public class PrimCase extends Function {

    private final int[] arities;
    private final boolean hasDefault;
    
    private PrimCase(int[] arities, boolean d) {
        super(getName(arities, d), arities.length + 1);
        this.arities = arities;
        this.hasDefault = d;
    }
    
    public static Function of(int[] arities, boolean d) {
        if (d) {
            if (arities.length == 2 && arities[0] == 0 && arities[1] == 0)
                return new Case0_();
        } else {
            if (arities.length == 2 && arities[0] == 0 && arities[1] == 2)
                return new Case02();
        }
        return new PrimCase(arities, d);
    }
    
    private static final String getName(int[] arities, boolean d) {
        StringBuilder b = new StringBuilder();
        b.append("Case{");
        for (int i = 0; i < arities.length; i++) {
            if (i != 0) {
                b.append(',');
            }
            if (d && i == arities.length - 1) {
                b.append(arities[i] == 0 ? '_' : '*');
            } else {
                b.append(arities[i]);
            }
        }
        b.append('}');
        return b.toString();
    }
    
    @Override
    Node exec(RedContext c) {
        int arity = getArity();
        
        Node redex = c.get(arity);
        c.setTos(redex.getArg());
        c.eval();
        Node x = c.getTos();
        int tag = x.getTag();
        if (hasDefault) {
            tag = Math.min(tag, arities.length - 1);
        }
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
        throw new RedException("case: unhandled tag " + tag);
    }

    private static final class Case0_ extends Function {

        public Case0_() {
            super("Case{0,_}", 3);
        }

        @Override
        Node exec(RedContext c) {
            c.setTos(c.getArg3());
            c.eval();
            Node result;
            if (c.getTos().getTag() == 0) {
                result = c.getArg1();
            } else {
                result = c.getArg2();
            }
            c.pop3();
            c.getTos().overwriteInd(result);
            c.setTos(result);
            return null;
/* Evalprojections:
        c.set2(result);
        c.pop2();
        c.get1().overwriteHole();
        c.eval();
        result = c.getTos();
        c.pop1();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return null;

 */
        }

    }

    private static class Case02 extends Function {
        
        public Case02() {
            super("Case{0,2}", 3);
        }

        @Override
        Node exec(RedContext c) {
            Node redex = c.get(3);
            c.setTos(redex.getArg());
            c.eval();
            Node x = c.getTos();
            Node alt;
            switch (x.getTag()) {
            case 0:
                // TODO: evalprojections
                alt = c.getArg1();
                redex.overwriteInd(alt);
                c.pop(3);
                c.setTos(alt);
                return null;
            case 1:
                alt = c.getArg2();
                alt = c.mkApp(alt, x.getField(0));
                redex.overwriteApp(alt, x.getField(1));
                c.pop(2);
                c.setTos(alt);
                return null;
            default:
                throw new RedException("case: unhandled tag " + x);
            }
        }
        
    }
    
}
