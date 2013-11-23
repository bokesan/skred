package de.bokeh.skred.red;

public class AppIndI extends Node {
    private Node fun, arg;

    public AppIndI(Node f, Node a) {
        assert f != null && a != null;
        fun = f;
        arg = a;
    }

    public boolean isApp() {
        return fun != null && fun != Function.I_FOR_IND;
    }
    
    public boolean isIndirection() {
        return fun == Function.I_FOR_IND;
    }
    
    public boolean isHole() {
        return fun == null;
    }
    
    public Node getFun() {
        return fun;
    }

    public Node getArg() {
        return arg;
    }

    public Node eval(RedContext c) {
        return c.unwind();
    }

    public Node unwind(RedContext c) {
        c.push(fun);
        return null;
    }

    @Override
    public void overwriteApp(Node f, Node a) {
        assert f != null && a != null;
        if (fun == Function.I_FOR_IND)
            throw new RedException("tried to overwrite ind node");
        this.fun = f;
        this.arg = a;
    }

    @Override
    public void overwriteInd(Node target) {
        assert target != null;
        //if (fun == Function.I_FOR_IND)
        //    throw new RedException("tried to overwrite ind node");
        if (target == this)
            throw new RedException("circular indirection!");
        this.fun = Function.I_FOR_IND;
        this.arg = target;
    }

    @Override
    public void overwriteHole() {
        fun = null;
        arg = null;
    }

    @Override
    public String toString(boolean parens, int d) {
        if (d <= 0) {
            return "?";
        }
        d--;
        if (fun == Function.I_FOR_IND)
            return "^" + fun.toString(true, d);
        if (arg != null) {
            if (parens) {
                return "(" + fun.toString(false, d) + " " + arg.toString(true, d) + ")";
            } else {
                return fun.toString(false, d) + " " + arg.toString(true, d);
            }
        }
        return "#HOLE";
    }

    @Override
    public boolean hasVars() {
        return arg.hasVars() || (fun != null && fun.hasVars());
    }

}
