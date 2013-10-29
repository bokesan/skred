package de.bokeh.skred.red;

public class PrimError extends Function {

    public PrimError() {
        super("error", 1);
    }

    @Override
    Node exec(RedContext c) {
        throw new RedException("error called with argument: " + c.getArg(1));
    }

}
