package de.bokeh.skred.red;

/**
 * Application nodes. Overwriting is implemented via a state pattern approach.
 */
public final class AppST extends Node {

    private NodeHandler state;
    private Node fun, arg;

    public AppST(Node f, Node a) {
        assert f != null && a != null;
        state = APP_HANDLER;
	fun = f;
	arg = a;
    }
    
    public boolean isApp() {
        return true;
    }

    public Node getFun() {
        return state.getFun(this);
    }

    public Node getArg() {
        return state.getArg(this);
    }

    public Node eval(RedContext c) {
        return state.eval(this, c);
    }

    public Node unwind(RedContext c) {
        return state.unwind(this, c);
    }

    @Override
    public void overwriteApp(Node f, Node a) {
        state.overwriteApp(this, f, a);
    }

    @Override
    public void overwriteInd(Node target) {
        state.overwriteInd(this, target);
    }

    @Override
    public void overwriteHole() {
        state.overwriteHole(this);
    }

    @Override
    public String toString() {
        return state.toString(this);
    }
   
    abstract private static class NodeHandler {
        abstract Node eval(AppST me, RedContext c);
        abstract Node unwind(AppST me, RedContext c);
        abstract Node getFun(AppST me);
        abstract Node getArg(AppST me);
        abstract void overwriteHole(AppST me);
        abstract void overwriteInd(AppST me, Node target);
        abstract void overwriteApp(AppST me, Node fun, Node arg);
        abstract String toString(AppST me);
    }

    private static final NodeHandler APP_HANDLER = new AppHandler();
    private static final NodeHandler IND_HANDLER = new IndHandler();
    private static final NodeHandler HOLE_HANDLER = new HoleHandler();
    
    private static final class AppHandler extends NodeHandler {
        Node eval(AppST me, RedContext c) {
            return c.unwind();
        }
        
        Node unwind(AppST me, RedContext c) {
            c.push(me.fun);
            return null;
        }

        @Override
        Node getArg(AppST me) {
            return me.arg;
        }

        @Override
        Node getFun(AppST me) {
            return me.fun;
        }

        @Override
        void overwriteApp(AppST me, Node fun, Node arg) {
            me.fun = fun;
            me.arg = arg;
        }

        @Override
        void overwriteHole(AppST me) {
            me.state = HOLE_HANDLER;
            me.fun = null;
            me.arg = null;
        }

        @Override
        void overwriteInd(AppST me, Node target) {
            if (target == me)
                throw new RedException("circular indirection");
            me.state = IND_HANDLER;
            me.fun = target;
            me.arg = null;
        }

        String toString(AppST me) {
            return "(" + me.fun + " " + me.arg + ")";
        }
    }
    
    private static final class IndHandler extends NodeHandler {
        Node eval(AppST me, RedContext c) {
            c.setTos(me.fun);
            return me.fun.eval(c);
        }
        
        Node unwind(AppST me, RedContext c) {
            c.setTos(me.fun);
            return null;
        }
        
        @Override
        Node getArg(AppST me) {
            throw new RedException("already updated");
        }

        @Override
        Node getFun(AppST me) {
            throw new RedException("already updated");
        }

        @Override
        void overwriteApp(AppST me, Node fun, Node arg) {
            throw new RedException("tried to overwrite ind node");
        }

        @Override
        void overwriteHole(AppST me) {
            throw new RedException("tried to overwrite ind node");
        }

        @Override
        void overwriteInd(AppST me, Node target) {
            throw new RedException("tried to overwrite ind node");
        }

        String toString(AppST me) {
            return "^" + me.fun;
        }
    }
    
    private static final class HoleHandler extends NodeHandler {
        
        @Override
        Node eval(AppST me, RedContext c) {
            throw new RedException("eval of hole");
        }

        @Override
        Node getArg(AppST me) {
            throw new RedException("hole accessed");
        }

        @Override
        Node getFun(AppST me) {
            throw new RedException("hole accessed");
        }

        @Override
        void overwriteApp(AppST me, Node fun, Node arg) {
            me.state = APP_HANDLER;
            me.fun = fun;
            me.arg = arg;
        }

        @Override
        void overwriteHole(AppST me) {
            throw new RedException("double hole");
        }

        @Override
        void overwriteInd(AppST me, Node target) {
            me.state = IND_HANDLER;
            me.fun = target;
        }

        @Override
        Node unwind(AppST me, RedContext c) {
            throw new RedException("unwind of hole");
        }

        String toString(AppST me) {
            return "#HOLE";
        }
    }
}
