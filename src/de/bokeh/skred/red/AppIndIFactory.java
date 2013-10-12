package de.bokeh.skred.red;

public class AppIndIFactory extends AppFactory {

    @Override
    protected Node newApp(Node fun, Node arg) {
        return new AppIndI(fun, arg);
    }

    @Override
    public String toString() {
        return "Application nodes implement overwriting with the I combinator";
    }
 
}
