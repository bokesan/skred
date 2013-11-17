package de.bokeh.skred.input;

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
    private final Map<String, Node> defns = new HashMap<String, Node>();
    private final Set<String> rewritten = new HashSet<String>();

    public AbstractSkReader(AppFactory appFactory) {
        this.appFactory = appFactory;
    }

    @Override
    public void addDefn(String name, Node value) {
        defns.put(name, value);
    }

    @Override
    public Node getGraph(String name) throws SkFileCorruptException {
        linkDefn(name);
        return defns.get(name);
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
            if (d == null) {
                d = Function.valueOf(e.toString());
            }
            if (d == null)
                throw new SkFileCorruptException("function " + e + " not found");
            if (!(d instanceof Function)) {
                linkDefn(e.toString());
                d = defns.get(e.toString());
                if (d.isApp()) {
                    return appFactory.mkApp(Function.I_FOR_IND, d);
                }
            }
            return d;
        }
        else if (e.isApp()) {
            e.overwriteApp(link(e.getFun()), link(e.getArg()));
            return e;
        }
        else
            return e;
    }

}