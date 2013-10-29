package de.bokeh.skred.input;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.bokeh.skred.red.AppFactory;
import de.bokeh.skred.red.Function;
import de.bokeh.skred.red.Node;
import de.bokeh.skred.red.Symbol;

abstract public class AbstractSkReader implements SkReader {

    protected final AppFactory appFactory;
    protected final Map<String, Node> defns = new HashMap<String, Node>();
    private final Set<String> rewritten = new HashSet<String>();

    public AbstractSkReader(AppFactory appFactory) {
        this.appFactory = appFactory;
    }
    
    //@Override
    abstract public void readDefns(File file) throws IOException;

    //@Override
    public Node getGraph() throws SkFileCorruptException {
        linkDefn("main");
        
        for (String s : defns.keySet()) {
            if (!rewritten.contains(s)) {
                System.out.println("warning: unused symbol '" + s + "'");
            }
        }
        
        return defns.get("main");
    }

    private void linkDefn(String name) throws SkFileCorruptException {
        if (rewritten.contains(name))
            return;
        Node d = defns.get(name);
        if (d == null)
            throw new SkFileCorruptException("undefined: " + name);
        rewritten.add(name);
        if (d instanceof Symbol)
            return;
        if (d.isApp()) {
            d.overwriteApp(link(d.getFun()), link(d.getArg()));
        }
    }

    private Node link(Node e) throws SkFileCorruptException {
        if (e instanceof Symbol) {
            Node d = defns.get(e.toString());
            while (d != null && (d instanceof Symbol)) {
                e = d;
                d = defns.get(e.toString());
            }
            boolean builtin = false;
            if (d == null) {
                d = Function.valueOf(e.toString());
                builtin = true;
            }
            if (d == null)
                throw new SkFileCorruptException("function " + e + " not found");
            if (!builtin) {
                linkDefn(e.toString());
                d = defns.get(e.toString());
            }
            return appFactory.mkApp(Function.I_FOR_IND, d);
        }
        else if (e.isApp()) {
            e.overwriteApp(link(e.getFun()), link(e.getArg()));
            return e;
        }
        else
            return e;
    }

}