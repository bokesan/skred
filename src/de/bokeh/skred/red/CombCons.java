package de.bokeh.skred.red;

public class CombCons extends Function {

    public CombCons() {
        super("cons", 2);
    }
    
    @Override
    Node exec(RedContext c) {
        Node redex = c.get(2);
        Node h = c.getArg(1);
        Node t = redex.getArg();
        Node result = Data.valueOf(1, h, t);
        c.pop2();
        redex.overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
