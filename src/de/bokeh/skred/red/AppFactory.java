package de.bokeh.skred.red;

public abstract class AppFactory {

    public Node mkApp(Node f, Node a) {
        if (f == Function.C) {
            if (a == Function.valueOf("add") || a == Function.valueOf("mul")
                    || a == Function.valueOf("eq") || a == Function.valueOf("neq"))
                return a;
            if (a == Function.valueOf("less")) return Function.valueOf("greater");
            if (a == Function.valueOf("greater")) return Function.valueOf("less");
            if (a == Function.valueOf("less_eq")) return Function.valueOf("gr_eq");
            if (a == Function.valueOf("gr_eq")) return Function.valueOf("less_eq");
            if (a == Function.valueOf("sub")) return Function.valueOf("Rsub");
            if (a == Function.valueOf("Rsub")) return Function.valueOf("sub");
            if (a == Function.valueOf("div")) return Function.valueOf("Rdiv");
            if (a == Function.valueOf("Rdiv")) return Function.valueOf("div");
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

    abstract protected Node newApp(Node fun, Node arg);
    
}
