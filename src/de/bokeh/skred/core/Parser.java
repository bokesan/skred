package de.bokeh.skred.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.bokeh.skred.core.Lexeme.Kind;
import de.bokeh.skred.input.AbstractSkReader;
import de.bokeh.skred.red.AppFactory;
import de.bokeh.skred.red.Data;
import de.bokeh.skred.red.Function;
import de.bokeh.skred.red.Int;
import de.bokeh.skred.red.Node;
import de.bokeh.skred.red.PrimCase;
import de.bokeh.skred.red.PrimPack;
import de.bokeh.skred.red.Symbol;

public class Parser extends AbstractSkReader {

    private final AppFactory appFactory;
    private final BA ba;
    
    private Lexer lex;
    
    private Lexeme currTok;
    
    private boolean verbose = true;
    
    public Parser(AppFactory appFactory) {
        super(appFactory);
        this.appFactory = appFactory;
        this.ba = new BA(appFactory);
    }

    private void skip() throws IOException {
        currTok = lex.nextToken();
    }
    
    
    @Override
    public void readDefns(File file) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        this.lex = new Lexer(in, file.getName());
        skip();
        for (;;) {
            if (!(currTok.kind == Kind.QVARID || currTok.kind == Kind.QCONID)) {
                syntaxError("definition expected");
            }
            String s = currTok.text;
            skip();
            List<String> args = new ArrayList<>();
            while (currTok.kind == Kind.QVARID || (currTok.kind == Kind.RESERVEDID && "_".equals(currTok.text))) {
                args.add(currTok.text);
                skip();
            }
            accept(Kind.RESERVEDOP, "=");
            Node e = expression();
            defns.put(s, ba.abs(args, e));
            if (verbose) {
                System.out.println(s + " = " + defns.get(s));
                //System.out.println("no BA: " + e);
            }
            if (currTok.kind != Kind.SEMI) {
                break;
            }
            skip();
        }
        in.close();
        if (currTok.kind != Kind.EOF) {
            syntaxError("EOF expected, but got " + currTok);
        }
    }

/*

expression --> expr5 relOp expr5
expr5      --> expr6 ( : expr6 )*
expr6      --> ( expr7 + - )* expr7
expr7      --> ( expr9 * / % )* expr9
expr9      --> expr10 ( . expr10)*

expr10     --> lambda let if case application

application --> aexp+

aexp        --> var con literal
              parens tuples lists
 
 */

    private static final String[] REL_OPS = {
        "==", "/=", "<", ">", "<=", ">="
    };

    private boolean isOp(String[] ops) {
        return (currTok.kind == Kind.QVARSYM || currTok.kind == Kind.QCONSYM || currTok.kind == Kind.RESERVEDOP)
                && Lexer.contains(ops, currTok.text);
    }
    
    private Node expression() throws IOException {
        Node e = expr5();
        if (isOp(REL_OPS)) {
            String op = currTok.text;
            skip();
            e = appFactory.mkApp(Function.valueOf(op), e, expr5());
        }
        return e;
    }

    private Node expr5() throws IOException {
        List<Node> es = new ArrayList<>();
        es.add(expr6());
        while (currTok.kind == Kind.RESERVEDOP && ":".equals(currTok.text)) {
            skip();
            es.add(expr6());
        }
        return rassoc((Function)PrimPack.of(1, 2), es);
    }
    
    private Node rassoc(Function f, List<Node> xs) {
        int n = xs.size();
        Node e = xs.get(n - 1);
        for (int i = n - 2; i >= 0; i--) {
            e = appFactory.mkApp(f, xs.get(i), e);
        }
        return e;
    }

    private Node expr6() throws IOException {
        Node e = expr7();
        while (isOp(new String[]{"+", "-"})) {
            String op = currTok.text;
            skip();
            e = appFactory.mkApp(Function.valueOf(op), e, expr7());
        }
        return e;
    }
    
    private Node expr7() throws IOException {
        Node e = expr9();
        while (isOp(new String[]{"*", "/", "%"})) {
            String op = currTok.text;
            skip();
            e = appFactory.mkApp(Function.valueOf(op), e, expr9());
        }
        return e;
    }
    
    private Node expr9() throws IOException {
        List<Node> es = new ArrayList<>();
        es.add(expr10());
        while (currTok.kind == Kind.QVARSYM && ".".equals(currTok.text)) {
            skip();
            es.add(expr10());
        }
        return rassoc(Function.valueOf("B"), es);
    }

    private Node expr10() throws IOException {
        switch (currTok.kind) {
        case RESERVEDOP:
            if ("\\".equals(currTok.text)) {
                return lambda();
            }
            break;
        case RESERVEDID:
            switch (currTok.text) {
            case "case":
                return caseExpression();
            case "if":
                return conditional();
            case "Pack":
                return application();
            default:
                break;
            }
        default:
            return application();
        }
        syntaxError("one of '\\', 'case', 'if', expression expected");
        return null;
    }

    private Node lambda() throws IOException {
        skip();
        List<String> xs = new ArrayList<>();
        while (currTok.kind == Kind.QVARID || (currTok.kind == Kind.RESERVEDID && "_".equals(currTok.text))) {
            xs.add(currTok.text);
            skip();
        }
        if (xs.isEmpty()) {
            syntaxError("identifier expexted");
        }
        accept(Kind.RESERVEDOP, "->");
        return ba.abs(xs, expression());
    }

    private Node application() throws IOException {
        Node e = aexpr(false);
        for (;;) {
            Node arg = aexpr(true);
            if (arg == null)
                break;
            e = appFactory.mkApp(e, arg);
        }
        return e;
    }
    
    private Node aexpr(boolean canFail) throws IOException {
        Node e;
        switch (currTok.kind) {
        case RESERVEDID:
            if (currTok.text.equals("Pack")) {
                return constructor();
            }
            break;
        case QVARID:
        case QCONID:
            e = Symbol.valueOf(currTok.text);
            skip();
            return e;
        case LIT_CHAR:
            e = Int.valueOf(currTok.text.codePointAt(0));
            skip();
            return e;
        case LIT_INT:
            e = Int.valueOf(Integer.parseInt(currTok.text));
            skip();
            return e;
        case LIT_STRING:
            e = makeString(currTok.text);
            skip();
            return e;
        case LPAREN:
            skip();
            e = expression();
            accept(Kind.RPAREN);
            return e;
        case LBRACKET:
            return parseList();
        default:
            break;
        }
        
        if (!canFail) {
            syntaxError("aexpr expected");
        }
        return null;
    }
    

    private Node parseList() throws IOException {
        // TODO
        skip();
        accept(Kind.RBRACKET);
        return PrimPack.of(0, 0);
    }

    private Node makeString(String s) {
        Node r = Data.valueOf(0);
        for (int i = s.length() - 1; i >= 0; i--) {
            int c = s.charAt(i);
            r = Data.valueOf(1, Int.valueOf(c), r);
        }
        return r;
    }

    
    private Node constructor() throws IOException {
        skip();
        accept(Kind.LBRACE);
        String t = currTok.text;
        accept(Kind.LIT_INT);
        accept(Kind.COMMA);
        String a = currTok.text;
        accept(Kind.LIT_INT);
        accept(Kind.RBRACE);
        return PrimPack.of(Integer.parseInt(t), Integer.parseInt(a));
    }

    private Node conditional() throws IOException {
        skip();
        Node p = expression();
        accept(Kind.RESERVEDID, "then");
        Node c = expression();
        accept(Kind.RESERVEDID, "else");
        Node a = expression();
        /*
        // if p then c else a ==> case p of { <0> -> a; <1> -> c }
        a = appFactory.mkApp(PrimUnpack.of(0), a);
        c = appFactory.mkApp(PrimUnpack.of(0), c);
        return appFactory.mkApp(PrimCase.of(2), a, c, p);
        */
        
        return appFactory.mkApp(Function.valueOf("if"), c, a, p);
    }

    private Node caseExpression() throws IOException {
        skip();
        Node e = expression();
        accept(Kind.RESERVEDID, "of");
        accept(Kind.LBRACE);

        Map<Integer, Alt> alts = new TreeMap<>();
        
        for (;;) {
            Alt a = alt();
            if (alts.put(a.tag, a) != null) {
                syntaxError("duplicate case tag");
            }
            if (currTok.kind == Kind.RBRACE){
                break;
            }
            if (currTok.kind != Kind.SEMI) {
                syntaxError("';' or '}' expected");
            }
            skip();
        }
        skip();
        
        Alt def = alts.remove(-1);
        int[] arities = new int[alts.size()];
        List<Node> altExprs = new ArrayList<>();
        int i = 0;
        for (Alt a : alts.values()) {
            if (a.tag != i) {
                syntaxError("missing tag in case: " + i);
            }
            rTrim(a.xs, "_");
            arities[i] = a.xs.size();
            altExprs.add(ba.abs(a.xs, a.e));
            i++;
        }

        Function c;
        if (def == null) {
            c = PrimCase.of(arities);
        } else {
            c = PrimCase.withDefault(arities);
            if (def.xs.get(0).equals("_")) {
                altExprs.add(appFactory.mkApp(Function.valueOf("K"), def.e));
            } else {
                altExprs.add(ba.abs(def.xs, def.e));
            }
        }
        altExprs.add(e);
        return appFactory.mkApp(c, altExprs);
    }
/*
 
  case foo of { <0> -> bar; <1> x xs -> quux }
  
  ===>
  
  case2 (unpack0 bar) (unpack2 (\x xs -> quux)) foo

  -
  
  unpack0 = const
  
  unpack2 f obj = f obj[0] obj[1]

*/

    private static <T> void rTrim(List<T> xs, T x) {
        int n = xs.size();
        while (n > 0 && xs.get(n - 1).equals(x)) {
            n--;
            xs.remove(n);
        }
    }
    
    
    
    
    
    private static class Alt {
        int tag;
        final List<String> xs = new ArrayList<>();
        Node e;
    }

    private Alt alt() throws IOException {
        Alt a = new Alt();
        if (currTok.kind == Kind.QVARID || (currTok.kind == Kind.RESERVEDID && "_".equals(currTok.text))) {
            a.xs.add(currTok.text);
            skip();
            a.tag = -1;
        } else {
            accept(Kind.QVARSYM, "<");
            String n = currTok.text;
            accept(Kind.LIT_INT);
            a.tag = Integer.parseInt(n);
            accept(Kind.QVARSYM, ">");
            while (currTok.kind == Kind.QVARID || (currTok.kind == Kind.RESERVEDID && "_".equals(currTok.text))) {
                a.xs.add(currTok.text);
                skip();
            }
        }
        accept(Kind.RESERVEDOP, "->");
        a.e = expression();
        return a;
    }
    
    
    private void accept(Kind kind) throws IOException {
        if (currTok.kind != kind) {
            syntaxError(kind + " expected");
        }
        currTok = lex.nextToken();
    }
    
    private void accept(Kind kind, String text) throws IOException {
        if (currTok.kind != kind || !text.equals(currTok.text)) {
            syntaxError(kind + " expected");
        }
        currTok = lex.nextToken();
    }
    
    private void syntaxError(String problem) {
        throw new SyntaxError(currTok, problem);
    }
    
}
