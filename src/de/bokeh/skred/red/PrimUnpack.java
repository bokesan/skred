package de.bokeh.skred.red;

import java.util.ArrayList;
import java.util.List;


public class PrimUnpack extends Function {

    private final int arity;
    
    private PrimUnpack(int arity) {
        super("unpack" + arity, 2);
        this.arity = arity;
    }
    
    private static final List<Function> uncons = new ArrayList<>();
    
    public static Function of(int arity) {
        int n = uncons.size();
        while (n <= arity) {
            if (n == 0) {
                uncons.add(Function.valueOf("K"));
            } else {
                uncons.add(new PrimUnpack(n));
            }
            n++;
        }
        return uncons.get(arity);
    }

    /*
     * unpack0 f C       = f
     * unpack1 f (C x)   = f x
     * unpack2 f (C x y) = f x y
     * ...
     */
    
    
    @Override
    Node exec(RedContext c) {
        // we assume that C is already evaluated
        Node con = c.getArg2();
        Node f = c.getArg1();
        Node redex = c.get2();
        if (con.getNumFields() != arity) {
            throw new RedException(this.toString() + ": wrong constructor arity: " + con.getNumFields());
        }

        if (arity == 0) {
            redex.overwriteInd(f);
            c.set2(f);
        } else {
            for (int i = 0; i < arity - 1; i++) {
                f = c.mkApp(f, con.getField(i));
            }
            redex.overwriteApp(f, con.getField(arity - 1));
        }
        c.pop2();
        return null;
    }

}
