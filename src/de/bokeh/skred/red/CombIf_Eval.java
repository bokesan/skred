package de.bokeh.skred.red;

public class CombIf_Eval extends Function {

    public CombIf_Eval() {
        super("if", 3);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg3());
        c.eval();
        Node result;
        if (c.getTos().getTag() == 0) {
            result = c.getArg2();
        } else {
            result = c.getArg1();
        }
        c.set2(result);
        c.pop2();
        c.get1().overwriteHole();
        c.eval();
        result = c.getTos();
        c.pop1();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return null;
    }

}
