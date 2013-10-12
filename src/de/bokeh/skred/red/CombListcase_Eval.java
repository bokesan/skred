package de.bokeh.skred.red;

public class CombListcase_Eval extends Function {

    public CombListcase_Eval() {
        super("_LISTCASE", 3);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg3());
        c.eval();
        Node x = c.getTos();
        Node r, g;
        switch (x.getTag()) {
        case 0:
            c.set2(c.getArg1());
            c.get3().overwriteHole();
            c.pop2();
            c.eval();
            r = c.getTos();
            c.pop1();
            c.getTos().overwriteInd(r);
            c.setTos(r);
            return null;
        case 1:
            c.pop1();
            r = c.get2();
            g = c.getArg1();
            Node a1 = c.mkApp(g, x.getField(0));
            r.overwriteApp(a1, x.getField(1));
            c.set1(a1);
            c.setTos(g);
            return null;
        default:
            throw new RedException("Listcase: unexpected tag " + x.getTag());
        }
    }

}
