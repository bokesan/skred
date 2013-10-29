package de.bokeh.skred.core;

public class Lexeme {

    public enum Kind {
        QVARID, QCONID, QVARSYM, QCONSYM,
        RESERVEDOP, RESERVEDID,
        
        LPAREN, RPAREN, LBRACE, RBRACE, RBRACKET, LBRACKET,
        COMMA, SEMI, BACKTICK,
        
        LIT_INT, LIT_FLOAT, LIT_CHAR, LIT_STRING,
        
        EOF
    }

    public final Kind kind;
    public final String text;
    
    public final int line;
    public final int column;
    public final String file;

    public Lexeme(Kind kind, String text, String file, int line, int column) {
        this.kind = kind;
        this.text = text;
        this.file = file;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return kind + (text != null ? " \"" + text + "\"" : "")
                + " [" + file + "(" + line + "," + column + ")]";
    }

}
