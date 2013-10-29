package de.bokeh.skred.red;

import java.util.List;

public abstract class AppFactory {

    public Node mkApp(Node f, Node a) {
        if (f == Function.C) {
            if (a == Function.valueOf("+") || a == Function.valueOf("*")
                    || a == Function.valueOf("==") || a == Function.valueOf("/="))
                return a;
            if (a == Function.valueOf("<")) return Function.valueOf(">");
            if (a == Function.valueOf(">")) return Function.valueOf("<");
            if (a == Function.valueOf("<=")) return Function.valueOf(">=");
            if (a == Function.valueOf(">=")) return Function.valueOf("<=");
            if (a == Function.valueOf("-")) return Function.valueOf("Rsub");
            if (a == Function.valueOf("Rsub")) return Function.valueOf("-");
            if (a == Function.valueOf("/")) return Function.valueOf("Rdiv");
            if (a == Function.valueOf("Rdiv")) return Function.valueOf("/");
            if (a == Function.valueOf("%")) return Function.valueOf("Rrem");
            if (a == Function.valueOf("Rrem")) return Function.valueOf("%");
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
