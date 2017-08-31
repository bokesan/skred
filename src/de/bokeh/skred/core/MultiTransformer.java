package de.bokeh.skred.core;

import java.util.ArrayList;
import java.util.List;

import de.bokeh.skred.red.AppFactory;
import de.bokeh.skred.red.Node;

public class MultiTransformer extends Transformer {

    private final List<Transformer> tfs = new ArrayList<>();
    
    public MultiTransformer(AppFactory appFactory) {
        super(appFactory);
    }
    
    public void add(Transformer t) {
        tfs.add(t);
    }
    
    @Override
    public int getCount() {
        int sum = 0;
        for (Transformer t : tfs) {
            sum += t.getCount();
        }
        return sum;
    }

    @Override
    protected Node apply(Node n) {
        boolean globalChange = false; 
        boolean changed;
        do {
            changed = false;
            for (Transformer t : tfs) {
                Node n1 = t.apply(n);
                if (n1 != null) {
                    changed = true;
                    globalChange = true;
                    n = n1;
                }
            }
        } while (changed);
        return globalChange ? n : null;
    }

}
