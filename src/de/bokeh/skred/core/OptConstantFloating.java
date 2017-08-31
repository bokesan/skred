package de.bokeh.skred.core;

import java.util.HashMap;
import java.util.Map;

import de.bokeh.skred.red.AppFactory;
import de.bokeh.skred.red.Function;
import de.bokeh.skred.red.Int;
import de.bokeh.skred.red.Node;

public class OptConstantFloating extends Transformer {

    private final Map<Node, Function> revOp = new HashMap<>();
    
    public OptConstantFloating(AppFactory appFactory) {
        super(appFactory);
        regOp("add");
        regOp("mul");
        regOp("sub", "Rsub");
        regOp("div", "Rdiv");
        regOp("rem", "Rrem");
        regOp("eq");
        regOp("ne");
        regOp("lt", "gt");
        regOp("le", "ge");
    }

    private void regOp(String fn1, String fn2) {
        Function f1 = Function.valueOf(fn1);
        Function f2 = Function.valueOf(fn2);
        revOp.put(f1,  f2);
        revOp.put(f2,  f1);
    }

    private void regOp(String f) {
        Function fun = Function.valueOf(f);
        revOp.put(fun, fun);
    }

    @Override
    protected Node apply(Node n) {
        if (!n.isApp()) return null;
        Node a1 = n.getFun();
        if (!a1.isApp()) return null;
        Node f = revOp.get(a1.getFun());
        if (f == null) return null;
        a1 = a1.getArg();
        if (a1 instanceof Int) return null;
        Node a2 = n.getArg();
        if (!(a2 instanceof Int)) return null;
        return appFactory.mkApp(f, a2, a1);
    }

}
