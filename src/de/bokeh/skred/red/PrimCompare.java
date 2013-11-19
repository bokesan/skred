package de.bokeh.skred.red;

public class PrimCompare extends Function {

    public PrimCompare() {
        super("compare", 2);
    }

    @Override
    Node exec(RedContext c) {
        c.rearrange2();
        c.eval();
        c.swap();
        c.eval();
        Node a2 = c.getTos();
        Node a1 = c.get1();
        int r = a1.intValue().compareTo(a2.intValue());
        Node result;
        if (r < 0) {
            result = Data.valueOf(0);
        } else if (r == 0) {
            result = Data.valueOf(1);
        } else {
            result = Data.valueOf(2);
        }
        c.pop2();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
