package de.bokeh.skred.input;

import java.io.IOException;
import java.io.Reader;

import de.bokeh.skred.red.Node;

public interface SkReader {

    public Node getGraph(String name) throws SkFileCorruptException;
    
    public void readDefns(Reader in, String fileName) throws IOException;
    
    public void addDefn(String name, Node value);
    
}
