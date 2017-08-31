package de.bokeh.skred.core;

import java.util.List;

import de.bokeh.skred.red.AppFactory;
import de.bokeh.skred.red.Function;
import de.bokeh.skred.red.Node;
import de.bokeh.skred.red.Symbol;

public class BA {
    
    private final AppFactory appFactory;
    private final boolean useBStar;
    private final Transformer opt1;

    public BA(AppFactory appFactory, boolean useBStar) {
        this.appFactory = appFactory;
        this.useBStar = useBStar;
        MultiTransformer t = new MultiTransformer(appFactory);
        t.add(new OptConstantFloating(appFactory));
        t.add(new OptEvalConstructors(appFactory));
        opt1 = t;
    }
    
    
    public Node abs(List<String> xs, Node e) {
        int n = xs.size();
        for (int i = n - 1; i >= 0; i--) {
            e = abs(xs.get(i), e);
        }
        return e;
    }
    
    public Node abs(String x, Node e) {
        if (x.equals("_")) {
            return appFactory.mkApp(Function.valueOf("K"), e); 
        }
        if (e.isApp()) {
            e = opt1.transform(e);
            return opt(abs(x, e.getFun()), abs(x, e.getArg()));
        } else {
            if (e instanceof Symbol && e.toString().equals(x)) {
                return Function.valueOf("I");
            } else {
                return appFactory.mkApp(Function.valueOf("K"), e); 
            }
        }
    }

    public Node absMany(List<String> xs, Node e) {
        int n = xs.size();
        if (n == 0) {
            return e;
        }
        Node e1 = absMany(xs.subList(1, n), e);
        Node e2 = abs(xs.get(0), e1);
        // return appFactory.mkApp(Symbol.valueOf("U"), e2);
        return appFactory.mkApp(Function.valueOf("S'"), e2, Function.primGet(0), Function.primGet(1));
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

            if (useBStar) {
                if (isB(a)) {
                    // S (K p) (B q r) = B* p q r
                    return appFactory.mkApp(Function.valueOf("Bs"), f.getArg(), a.getFun().getArg(), a.getArg());
                }
            } else {
                if (f.getArg().isApp()) {
                    // S (K (p q)) r = B' p q r
                    return appFactory.mkApp(Function.valueOf("B'"), f.getArg().getFun(), f.getArg().getArg(), a);
                }
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
