package de.bokeh.skred.red;


public class PrimGet extends Function {

    private final int index;
    
    public PrimGet(int index) {
        super("Get{" + index + "}", 1);
        this.index = index;
    }
    
    @Override
    Node exec(RedContext c) {
        Node con = c.getArg1();
        c.setTos(con);
        c.eval();
        con = c.getTos();
        Node redex = c.get1();
        if (con.getNumFields() <= index) {
            throw new RedException(this.toString() + ": field index out of range: " + con.getNumFields());
        }
        // TODO: evalprojections
        redex.overwriteInd(con.getField(index));
        c.pop1();
        return null;
    }

}
