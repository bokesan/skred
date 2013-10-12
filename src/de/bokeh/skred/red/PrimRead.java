package de.bokeh.skred.red;

public class PrimRead extends Function {

    public PrimRead() {
        super("_READ", 1);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg(1));
        c.eval();
        InputPort p = (InputPort) c.getTos();
        c.pop1();
        int ch = p.read();
        Node result;
        if (ch < 0) {
            result = Data.valueOf(0);
        } else {
            result = Data.valueOf(1, Int.valueOf(ch), c.mkApp(this, p));
        }
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }
 
}
