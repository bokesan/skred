package de.bokeh.skred.red;

import java.util.List;

public abstract class AppFactory {

    public Node mkApp(Node f, Node a) {
        if (f == Function.C) {
            if (a == Function.valueOf("add") || a == Function.valueOf("mul")
                    || a == Function.valueOf("eq") || a == Function.valueOf("ne"))
                return a;
            if (a == Function.valueOf("lt")) return Function.valueOf("gt");
            if (a == Function.valueOf("gt")) return Function.valueOf("lt");
            if (a == Function.valueOf("le")) return Function.valueOf("ge");
            if (a == Function.valueOf("ge")) return Function.valueOf("le");
            if (a == Function.valueOf("sub")) return Function.valueOf("Rsub");
            if (a == Function.valueOf("Rsub")) return Function.valueOf("sub");
            if (a == Function.valueOf("quot")) return Function.valueOf("Rquot");
            if (a == Function.valueOf("Rquot")) return Function.valueOf("quot");
            if (a == Function.valueOf("rem")) return Function.valueOf("Rrem");
            if (a == Function.valueOf("Rrem")) return Function.valueOf("rem");
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
