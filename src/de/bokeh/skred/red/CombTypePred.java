package de.bokeh.skred.red;

public class CombTypePred extends Function {

    private enum Type { BOOL, CHAR, NUMBER, PAIR }
    
    private final Type type;
    
    public CombTypePred(String name) {
        super(name, 1);
        if (name.equals("boolean"))
            type = Type.BOOL;
        else if (name.equals("char"))
            type = Type.CHAR;
        else if (name.equals("number"))
            type = Type.NUMBER;
        else if (name.equals("pair"))
            type = Type.PAIR;
        else
            throw new IllegalArgumentException("unknown type: " + name);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg(1));
        c.eval();
        Node x = c.getTos();
        boolean r;
        switch (type) {
        case CHAR: r = (x instanceof Int); break;
        case NUMBER: r = (x instanceof Int); break;
        case BOOL:
            if (x instanceof Data) {
                Data x1 = (Data) x;
                r = x1.getNumFields() == 0 && x1.getTag() <= 1;
            } else {
                r = false;
            }
            break;
        case PAIR:
            if (x instanceof Data) {
                Data x1 = (Data) x;
                r = (x1.getNumFields() == 0 && x1.getTag() == 0)
                    || (x1.getNumFields() == 2 && x1.getTag() == 1);
            } else {
                r = false;
            }
            break;
        default:
            throw new AssertionError(type);
        }
        Node result = Data.valueOf(r ? 1 : 0);
        c.pop1();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return result;
    }

}
