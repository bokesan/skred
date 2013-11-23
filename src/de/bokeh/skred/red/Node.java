package de.bokeh.skred.red;

import java.math.BigInteger;

/**
 * A graph node.
 */
public abstract class Node {

    abstract public Node eval(RedContext c);

    abstract public Node unwind(RedContext c);
    
    abstract public void overwriteApp(Node f, Node a);
    
    abstract public void overwriteInd(Node target);
    
    abstract public void overwriteHole();
    
    abstract public Node getArg();
    
    abstract public String toString(boolean parens, int maxDepth);
    
    abstract public boolean hasVars();
    
    @Override
    public String toString() {
        return toString(false, 12);
    }

    public Node getFun() {
        throw new RedException("not an application node: " + this);
    }
    
    public BigInteger intValue() {
        throw new RedException("intValue not defined: " + this);
    }
    
    public double doubleValue() {
        throw new RedException("doubleValue not defined: " + this);
    }
    
    public boolean isApp() {
        return false;
    }
    
    public boolean isIndirection() {
        return false;
    }
    
    public boolean isHole() {
        return false;
    }
    
    public int getTag() {
        throw new RedException("getTag not defined: " + this);
    }
    
    public Node getField(int n) {
        throw new RedException("getField not defined: " + this);
    }
    
    public int getNumFields() {
        throw new RedException("getNumFields not defined: " + this);
    }
}
