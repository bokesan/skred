package de.bokeh.skred.red;

import java.util.HashMap;
import java.util.Map;

public class PrimPack extends Function {

    private final int tag;
    private final int arity;
    
    private PrimPack(int tag, int arity) {
        super("Pack{" + tag + "," + arity + "}", arity);
        this.tag = tag;
        this.arity = arity;
    }
    
    private static final Map<Integer, Map<Integer, Node>> consByTag = new HashMap<>();
    
    public static Node of(int tag, int arity) {
        Map<Integer, Node> byArity = consByTag.get(tag);
        if (byArity == null) {
            byArity = new HashMap<>();
            consByTag.put(tag, byArity);
        }
        Node p = byArity.get(arity);
        if (p == null) {
            if (arity == 0) {
                p = Data.valueOf(tag);
            } else {
                p = new PrimPack(tag, arity);
            }
            byArity.put(arity, p);
        }
        return p;
    }
    
    
    @Override
    Node exec(RedContext c) {
        Node redex, result;
        if (arity == 0) {
            redex = c.getTos();
            result = Data.valueOf(tag);
        } else {
            Node[] args = new Node[arity];
            for (int k = 1; k <= arity; k++) {
                args[k-1] = c.getArg(k);
            }
            redex = c.get(arity);
            result = Data.valueOf(tag, args);
            c.pop(arity);
        }
        redex.overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
