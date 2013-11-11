package de.bokeh.skred.red;

import java.util.List;

public abstract class AppFactory {

    private final boolean optimize;
    
    public AppFactory(boolean optimize) {
        this.optimize = optimize;
    }


    private static final String[] REVERSE_OPS = {
        "add", "add", "mul", "mul", "eq", "eq", "ne", "ne",
        "lt", "gt", "le", "ge", "sub", "Rsub",
        "quot", "Rquot", "rem", "Rrem"
    };
    
    private static String getReverseOp(String op) {
        for (int i = 0; i < REVERSE_OPS.length; i += 2) {
            if (REVERSE_OPS[i].equals(op)) {
                return REVERSE_OPS[i+1];
            }
            if (REVERSE_OPS[i+1].equals(op)) {
                return REVERSE_OPS[i];
            }
        }
        return null;
    }

    public Node mkApp(Node f, Node a) {
        if (optimize) {
            if (f == Function.C && a instanceof Function) {
                String r = getReverseOp(((Function)a).getName());
                if (r != null) {
                    return Function.valueOf(r);
                }
            }
            else if (f == Function.B && a == Function.B) {
                return Function.valueOf("B'");
            }
            else if ((f == Function.getK() && a == Function.getI())
                    || (f == Function.S && a == Function.getK())
                    || (f == Function.C && a == Function.getK())) {
                return Function.getK1();
            }
            else if (f == Function.getK1()) {
                return Function.getI();
            }
        }
        return newApp(f, a);
    }
    
    public Node mkApp(Node f, Node a1, Node a2) {
        return mkApp(mkApp(f, a1), a2);
    }
    
    public Node mkApp(Node f, Node a1, Node a2, Node a3) {
        return mkApp(mkApp(mkApp(f, a1), a2), a3);
    }
    
    public Node mkApp(Node f, Node a1, Node a2, Node a3, Node a4) {
        return mkApp(mkApp(mkApp(mkApp(f, a1), a2), a3), a4);
    }
    
    public Node mkApp(Node f, List<Node> args) {
        for (Node a : args) {
            f = mkApp(f, a);
        }
        return f;
    }

    abstract protected Node newApp(Node fun, Node arg);
    
}
