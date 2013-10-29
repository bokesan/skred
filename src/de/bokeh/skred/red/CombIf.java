package de.bokeh.skred.red;

/**
 * If combinator.
 * <p>
 * if p c a === case p of { 0 -&gt; a ; 1 -&gt; c }
 */
public class CombIf extends Function {

    public CombIf() {
        super("if", 3);
    }

    @Override
    Node exec(RedContext c) {
        c.setTos(c.getArg3());
        c.eval();
        Node result;
        switch (c.getTos().getTag()) {
        case 0: result = c.getArg2(); break;
        case 1: result = c.getArg1(); break;
        default: throw new RedException("type error: unexpected tag " + c.getTos());
        }
        c.pop3();
        c.getTos().overwriteInd(result);
        c.setTos(result);
        return null;
    }

}
