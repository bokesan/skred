package de.bokeh.skred.core;

import de.bokeh.skred.red.AppFactory;
import de.bokeh.skred.red.Node;

public abstract class Transformer {

    protected final AppFactory appFactory;
    private int count = 0;
    
    public Transformer(AppFactory appFactory) {
        this.appFactory = appFactory;
    }

    public Node transform(Node n) {
        if (n.isApp()) {
            Node f = transform(n.getFun());
            Node a = transform(n.getArg());
            if (f != null || a != null) {
                if (f == null) { f = n.getFun(); }
                if (a == null) { a = n.getArg(); }
                n = appFactory.mkApp(f, a);
            }
        }
        Node n1 = apply(n);
        if (n1 != null) {
            count++;
            return n1;
        }
        return n;
    }
    
    abstract protected Node apply(Node n);

    public int getCount() {
        return count;
    }
    
}
