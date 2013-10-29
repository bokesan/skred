package de.bokeh.skred.core;

@SuppressWarnings("serial")
public class SyntaxError extends RuntimeException {

    public SyntaxError(Lexeme t, String problem) {
        super("Syntax error in " + t.file + "(" + t.line + "," + t.column + "): " + problem);
    }

    public SyntaxError(String file, int line, int column, String problem) {
        super("Lexical error in " + file + "(" + line + "," + column + "): " + problem);
    }
    
}
