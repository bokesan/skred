package de.bokeh.skred.red;


public class PrimPutChar extends Function {

    public PrimPutChar() {
        super("hPutChar", 3);
    }

    @Override
    Node exec(RedContext c) {
        c.rearrange2();
        c.eval();
        c.swap();
        c.eval();
        Node a2 = c.getTos();
        OutputPort p = (OutputPort) c.get1();
        int ch = a2.intValue().intValue();
        p.getPrintStream().print((char)ch);
        Node result = Data.valueOf(0, Data.valueOf(0), Int.valueOf(0));
        c.pop3();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
