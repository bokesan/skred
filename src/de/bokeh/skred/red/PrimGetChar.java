package de.bokeh.skred.red;

public class PrimGetChar extends Function {

    public PrimGetChar() {
        super("hGetChar", 2);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg(1));
        c.eval();
        InputPort p = (InputPort) c.getTos();
        c.pop2();
        int ch = p.read();
        Node result;
        if (ch < 0) {
            result = Data.valueOf(1, Data.valueOf(1), Int.valueOf(0));
        } else {
            result = Data.valueOf(0, Int.valueOf(ch), Int.valueOf(0));
        }
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
