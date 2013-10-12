package de.bokeh.skred.input;

import java.io.File;
import java.io.IOException;
import de.bokeh.skred.red.Node;

public interface SkReader {

    public Node getGraph() throws SkFileCorruptException;
    
    public void readDefns(File file) throws IOException;
    
}
