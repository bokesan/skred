package de.bokeh.skred.core;

import de.bokeh.skred.red.AppFactory;
import de.bokeh.skred.red.Data;
import de.bokeh.skred.red.Node;
import de.bokeh.skred.red.PrimPack;

public class OptEvalConstructors extends Transformer {

    public OptEvalConstructors(AppFactory appFactory) {
        super(appFactory);
    }

    @Override
    protected Node apply(Node n) {
        Node f = n;
        int arity = 0;
        while (f.isApp()) {
            arity++;
            f = f.getFun();
        }
        if (!(f instanceof PrimPack)) return null;
        PrimPack p = (PrimPack) f;
        if (p.getArity() != arity) return null;
        Node[] fields = new Node[arity];
        for (int i = arity - 1; i >= 0; i--) {
            if (n.getArg().hasVars())
                return null;
            fields[i] = n.getArg();
            n = n.getFun();
        }
        return Data.valueOf(p.getTag(), fields);
    }

}
