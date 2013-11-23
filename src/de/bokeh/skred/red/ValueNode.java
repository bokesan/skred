package de.bokeh.skred.red;

public abstract class ValueNode extends Node {

    @Override
    public Node eval(RedContext c) {
        return this;
    }

    @Override
    public Node unwind(RedContext c) {
        if (c.numArgs() != 0)
            throw new RedException("tried to apply Value " + this + " to " + c.numArgs() + " arguments");
        return this;
    }

    @Override
    public Node getArg() {
        throw new RedException("getArg of value");
    }

    @Override
    public void overwriteApp(Node f, Node a) {
        throw new RedException("tried to overwrite value");
    }

    @Override
    public void overwriteInd(Node target) {
        throw new RedException("tried to overwrite value");
    }

    @Override
    public void overwriteHole() {
        throw new RedException("tried to overwrite value");
    }

    @Override
    public boolean hasVars() {
        return false;
    }

}
