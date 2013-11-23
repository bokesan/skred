package de.bokeh.skred.core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import de.bokeh.skred.core.Lexeme.Kind;

public class Lexer {

    private int line, column;
    private int markedLine = 0;
    private int markedColumn;
    
    private final String fileName;
    private final Reader in;
    
    private final int EOF = -1;
    
    private int currChar;
    
    public Lexer(Reader in, String fileName) throws IOException {
        this.in = in;
        this.fileName = fileName;
        line = 1;
        column = 1;
        currChar = in.read();
    }

    public Lexeme nextToken() throws IOException {
        for (;;) {
            int c = currChar;
            if (c == EOF) {
                return l(Kind.EOF);
            }
            mark();
            switch (CharClass.classify(c)) {
            case SPECIAL:
                nextChar();
                switch (c) {
                case '(': return l(Kind.LPAREN);
                case ')': return l(Kind.RPAREN);
                case '[': return l(Kind.LBRACKET);
                case ']': return l(Kind.RBRACKET);
                case '}': return l(Kind.RBRACE);
                case '{': 
                          if (currChar == '-') {
                              skipNestedComment();
                              break;
                          }
                          return l(Kind.LBRACE);
                case ',': return l(Kind.COMMA);
                case ';': return l(Kind.SEMI);
                case '`': return l(Kind.BACKTICK);
                default: throw new AssertionError();
                }
            case WHITESPACE:
                nextChar();
                break;
            case NUMBER:
                return lexNumber();
            case ID:
                return lexId();
            case SYMBOL:
                Lexeme tok = lexOp();
                if (tok != null) {
                    return tok;
                }
                // was an end-of-line comment
                break;
            case STRING:
                return lexString();
            case CHAR:
                return lexChar();
            }
        }
    }

    private Lexeme lexId() throws IOException {
        StringBuilder b = new StringBuilder();
        b.appendCodePoint(currChar);
        for (;;) {
            int c = nextChar();
            CharClass cls = CharClass.classify(c);
            if (cls != CharClass.ID && !Character.isDigit(c) && cls != CharClass.CHAR) {
                break;
            }
            b.appendCodePoint(c);
        }
        String id = b.toString();
        if (isReservedId(id)) {
            return l(Kind.RESERVEDID, id);
        }
        if (Character.isUpperCase(id.charAt(0))) {
            return l(Kind.QCONID, id);
        }
        return l(Kind.QVARID, id);
    }

    private void skipNestedComment() {
        // TODO Auto-generated method stub
        
    }

    private Lexeme lexNumber() throws IOException {
        StringBuilder b = new StringBuilder();
        for (;;) {
            b.appendCodePoint(currChar);
            nextChar();
            if (!Character.isDigit(currChar))
                break;
        }
        return l(Kind.LIT_INT, b.toString());
    }

    private Lexeme lexOp() throws IOException {
        StringBuilder b = new StringBuilder();
        b.appendCodePoint(currChar);
        for (;;) {
            int c = nextChar();
            if (CharClass.classify(c) != CharClass.SYMBOL) {
                break;
            }
            b.appendCodePoint(c);
        }
        String op = b.toString();
        if (isDashes(op)) {
            skipLineComment();
            return null;
        }
        if (isReservedOp(op)) {
            return l(Kind.RESERVEDOP, op);
        }
        if (op.charAt(0) == ':') {
            return l(Kind.QCONSYM, op);
        }
        return l(Kind.QVARSYM, op);
    }

    private void skipLineComment() throws IOException {
        while (currChar != '\n' && currChar != EOF) {
            nextChar();
        }
    }

    private static boolean isDashes(String op) {
        int n = op.length();
        if (n < 2) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (op.charAt(i) != '-') {
                return false;
            }
        }
        return true;
    }

    private Lexeme lexChar() throws IOException {
        int c = nextChar();
        if (c == '\\') {
            c = nextChar();
            switch (c) {
            case 'n': c = '\n'; break;
            case 't': c = '\t'; break;
            }
        }
        nextChar();
        accept('\'');
        return l(Kind.LIT_CHAR, String.valueOf((char)c));
    }

    private Lexeme lexString() throws IOException {
        StringBuilder b = new StringBuilder();
        for (;;) {
            int c = nextChar();
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                c = nextChar();
                switch (c) {
                case 'n': c = '\n'; break;
                case 't': c = '\t'; break;
                }
            }
            b.appendCodePoint(c);
        }
        accept('"');
        return l(Kind.LIT_STRING, b.toString());
    }


    private enum CharClass {
        SYMBOL, SPECIAL, WHITESPACE, NUMBER, ID, STRING, CHAR;
        
        public static CharClass classify(int c) {
            switch (c) {
            case '\'': return CHAR;
            case '"': return STRING;
            case '!': case '#': case '$': case '%': case '&': case '*': case '+':
            case '.': case '/': case '<': case '=': case '>': case '?': case '@':
            case '\\': case '^': case '|': case '-': case '~': case ':':
                return SYMBOL;
            case '(': case ')': case '[': case ']': case '{': case '}':
            case ',': case ';': case '`':
                return SPECIAL;
            case ' ': case '\t': case '\n': case '\r':
            case '\f':
                return WHITESPACE;
            default:
                if (Character.isWhitespace(c)) {
                    return WHITESPACE;
                }
                if (Character.isDigit(c)) {
                    return NUMBER;
                }
                if (Character.isAlphabetic(c) || c == '_') {
                    return ID;
                }
                throw new IllegalArgumentException("unknown character class: " + (char)c);
            }
        }
    }
    
    
    
    private int nextChar() throws IOException {
        switch (currChar) {
        case '\n':
            line++;
            column = 1;
            break;
        case '\r': case EOF:
            break;
        default:
            column++;
            break;
        }
        currChar = in.read();
        return currChar;
    }
    
    private void accept(char tok) throws IOException {
        if (currChar != tok) {
            syntaxError("'" + tok + "' expected");
        }
        nextChar();
    }

    private void syntaxError(String problem) {
        int ln, c;
        if (markedLine > 0) {
            ln = markedLine;
            c = markedColumn;
        } else {
            ln = line;
            c = column;
        }
        markedLine = 0;
        throw new SyntaxError(fileName, ln, c, problem);
    }


    private Lexeme l(Kind kind, String text) {
        assert markedLine > 0;
        Lexeme r = new Lexeme(kind, text, fileName, markedLine, markedColumn);
        markedLine = 0;
        return r;
    }

    private Lexeme l(Kind kind) {
        return l(kind, null);
    }

    private static final String[] RESERVED_OPS = {
        "..", ":", "::", "=", "\\", "|", "<-", "->",
        "@", "~", "=>"
    };
    
    private static final String[] RESERVED_IDS = {
        "case", "class", "data", "default", "deriving", "do", "else",
        "foreign", "if", "import", "in", "infix", "infixl",
        "infixr", "instance", "let", "module", "newtype", "of",
        "then", "type", "where", "_",
        "Pack", "Get", "letrec"
    };
    
    private static boolean isReservedOp(String op) {
        return contains(RESERVED_OPS, op);
    }
    
    private static boolean isReservedId(String id) {
        return contains(RESERVED_IDS, id);
    }
    
    public static boolean contains(String[] ids, String s) {
        for (String id : ids) {
            if (id.equals(s)) {
                return true;
            }
        }
        return false;
    }
    
    private void mark() {
        this.markedLine = line;
        this.markedColumn = column;
    }
    
    public static void main(String[] args) throws IOException {
        String fileName = args[0];
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        Lexer lex = new Lexer(in, fileName);
        for (;;) {
            Lexeme tok = lex.nextToken();
            if (tok.kind == Kind.EOF)
                break;
            System.out.println(tok.toString());
        }
        in.close();
    }
    
}
