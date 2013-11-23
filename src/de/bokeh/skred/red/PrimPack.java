package de.bokeh.skred.red;


public class PrimPack extends Function {

    private final int tag;
    private final int arity;
    
    public PrimPack(int tag, int arity) {
        super("Pack{" + tag + "," + arity + "}", arity);
        if (arity <= 0) {
            throw new IllegalArgumentException("PrimPack: invalid arity: " + arity);
        }
        this.tag = tag;
        this.arity = arity;
    }
    
    
    @Override
    Node exec(RedContext c) {
        Node redex, result;
        if (arity == 2) {
            Node f1 = c.getArg1();
            Node f2 = c.getArg2();
            redex = c.get2();
            result = Data.valueOf(tag, f1, f2);
            c.pop2();
        } else {
            Node[] args = new Node[arity];
            for (int k = 1; k <= arity; k++) {
                args[k-1] = c.getArg(k);
            }
            redex = c.get(arity);
            result = Data.valueOf(tag, args);
            c.pop(arity);
        }
        redex.overwriteInd(result);
        c.setTos(result);
        return result;
    }


    public int getTag() {
        return tag;
    }

}
