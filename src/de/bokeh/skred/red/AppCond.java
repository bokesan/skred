package de.bokeh.skred.red;

/**
 * Application nodes implementing overwriting with conditionals.
 */
public final class AppCond extends Node {

    // both null: hole, arg null: ind, none null: app
    private Node fun, arg;

    public AppCond(Node f, Node a) {
        assert f != null && a != null;
	fun = f;
	arg = a;
    }

    public boolean isApp() {
        return arg != null && fun != null;
    }
    
    public boolean isIndirection() {
        return arg == null && fun != null;
    }
    
    public boolean isHole() {
        return fun == null;
    }
    
    public Node getFun() {
        if (arg != null)
            return fun;
        if (fun != null)
            throw new RedException("already updated");
        throw new RedException("black hole");
    }

    public Node getArg() {
        if (arg != null)
            return arg;
        if (fun != null)
            throw new RedException("already updated");
        throw new RedException("black hole");
    }

    public Node eval(RedContext c) {
        if (arg != null)
            return c.unwind();
        if (fun != null) {
            c.setTos(fun);
            return fun.eval(c);
        }
        throw new RedException("hole found on spine[tos]");
    }

    public Node unwind(RedContext c) {
        // System.err.println("unwinding: " + this);
        if (arg != null)
            c.push(fun);
        else if (fun != null)
            c.setTos(fun);
        else
            throw new RedException("unwinding hole");
	return null;
    }

    @Override
    public void overwriteApp(Node f, Node a) {
        assert f != null && a != null;
        if (fun != null && arg == null)
            throw new RedException("tried to overwrite ind node");
        this.fun = f;
        this.arg = a;
    }

    @Override
    public void overwriteInd(Node target) {
        assert target != null;
        if (fun != null && arg == null)
            throw new RedException("tried to overwrite ind node");
        if (target == this)
            throw new RedException("circular indirection!");
        this.fun = target;
        this.arg = null;
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
        if (arg != null) {
            if (parens) {
                return "(" + fun.toString(false, d) + " " + arg.toString(true, d) + ")";
            } else {
                return fun.toString(false, d) + " " + arg.toString(true, d);
            }
        }
        if (fun != null)
            return "^" + fun.toString(true, d);
        return "#HOLE";
    }
   
}
