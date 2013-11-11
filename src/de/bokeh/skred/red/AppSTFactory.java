package de.bokeh.skred.red;

public class AppSTFactory extends AppFactory {

    public AppSTFactory(boolean optimize) {
        super(optimize);
    }

    @Override
    protected Node newApp(Node fun, Node arg) {
        return new AppST(fun, arg);
    }

    @Override
    public String toString() {
        return "Application nodes implement overwriting with state pattern";
    }

}
