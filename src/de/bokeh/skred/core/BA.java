package de.bokeh.skred.core;

import java.util.List;

import de.bokeh.skred.red.AppFactory;
import de.bokeh.skred.red.Function;
import de.bokeh.skred.red.Node;
import de.bokeh.skred.red.Symbol;

public class BA {
    
    private final AppFactory appFactory;

    public BA(AppFactory appFactory) {
        this.appFactory = appFactory;
    }
    
    
    public Node abs(List<String> xs, Node e) {
        int n = xs.size();
        for (int i = n - 1; i >= 0; i--) {
            e = abs(xs.get(i), e);
        }
        return e;
    }
    
    public Node abs(String x, Node e) {
        if (e.isApp()) {
            return opt(abs(x, e.getFun()), abs(x, e.getArg()));
        } else {
            if (e instanceof Symbol && e.toString().equals(x)) {
                return Function.valueOf("I");
            } else {
                return appFactory.mkApp(Function.valueOf("K"), e); 
            }
        }
    }
    
    private Node opt(Node f, Node a) {
        if (isK(f)) {
            if (isK(a)) {
                // S (K p) (K q) = K (p q)
                return appFactory.mkApp(Function.valueOf("K"), appFactory.mkApp(f.getArg(), a.getArg()));
            }
            if (a == Function.valueOf("I")) {
                // S (K p) I = p
                return f.getArg();
            }
            
            if (isB(a)) {
                // S (K p) (B q r) = B* p q r
                return appFactory.mkApp(Function.valueOf("B*"), f.getArg(), a.getFun().getArg(), a.getArg());
            }
            
            // S (K p) q = B p q
            return appFactory.mkApp(Function.valueOf("B"), f.getArg(), a);
        }

        if (isB(f)) {
            if (isK(a)) {
                // S (B p q) (K r) = C' p q r 
                return appFactory.mkApp(Function.valueOf("C'"), f.getFun().getArg(), f.getArg(), a.getArg());
            }
            // S (B p q) r = S' p q r
            return appFactory.mkApp(Function.valueOf("S'"), f.getFun().getArg(), f.getArg(), a);
        }
        
        if (isK(a)) {
            return appFactory.mkApp(Function.valueOf("C"), f, a.getArg());
        }
        
        if (a == Function.valueOf("I")) {
            return appFactory.mkApp(Function.valueOf("W"), f);
        }
        
        return appFactory.mkApp(Function.valueOf("S"), f, a);
    }
    
    private static boolean isK(Node e) {
        return e.isApp() && e.getFun() == Function.valueOf("K");
    }
    
    private static boolean isB(Node e) {
        return e.isApp() && e.getFun().isApp() && e.getFun().getFun() == Function.valueOf("B");
    }
    
}
