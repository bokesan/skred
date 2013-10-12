package de.bokeh.skred.red;

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
    
    public Node getFun() {
        throw new RedException("not an application node");
    }
    
    public int intValue() {
        throw new RedException("intValue not defined");
    }
    
    public double doubleValue() {
        throw new RedException("doubleValue not defined");
    }
    
    public boolean isApp() {
        return false;
    }
    
    public int getTag() {
        throw new RedException("getTag not defined");
    }
    
    public Node getField(int n) {
        throw new RedException("getField not defined");
    }
}
