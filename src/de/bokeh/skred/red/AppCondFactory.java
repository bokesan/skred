package de.bokeh.skred.red;

public class AppCondFactory extends AppFactory {

    public AppCondFactory(boolean optimize) {
        super(optimize);
    }

    @Override
    protected Node newApp(Node fun, Node arg) {
        return new AppCond(fun, arg);
    }

    @Override
    public String toString() {
        return "Application nodes implement overwriting with conditionals";
    }
    
}
