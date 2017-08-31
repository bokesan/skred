package de.bokeh.skred.core;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
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
import de.bokeh.skred.red.Symbol;

public class Parser extends AbstractSkReader {

    private final AppFactory appFactory;
    private final BA ba;
    
    private Lexer lex;
    
    private Lexeme currTok;
    
    public Parser(AppFactory appFactory, boolean useBStar) {
        super(appFactory);
        this.appFactory = appFactory;
        this.ba = new BA(appFactory, useBStar);
    }

    private void skip() throws IOException {
        currTok = lex.nextToken();
    }
    
    
    @Override
    public void readDefns(Reader in, String fileName) throws IOException {
        this.lex = new Lexer(in, fileName);
        skip();
        Map<String, Node> ds = defns();
        in.close();
        if (currTok.kind != Kind.EOF) {
            syntaxError("EOF expected, but got " + currTok);
        }
        ds.forEach(super::addDefn);
    }

    private Map<String, Node> defns() throws IOException {
        Map<String, Node> ds = new HashMap<>();
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
            e = ba.abs(args, e);
            e = ds.put(s, e);
            if (e != null) {
                syntaxError("duplicate definition: '" + s + "'");
            }
            if (currTok.kind != Kind.SEMI) {
                break;
            }
            skip();
        }
        return ds;
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
    
    private static String relopName(String sym) {
        switch (sym) {
        case "==": return "eq";
        case "/=": return "ne";
        case "<" : return "lt";
        case ">" : return "gt";
        case "<=": return "le";
        case ">=": return "ge";
        default:
            throw new IllegalArgumentException("invalid relop: " + sym);
        }
    }

    private boolean isOp(String[] ops) {
        return (currTok.kind == Kind.QVARSYM || currTok.kind == Kind.QCONSYM || currTok.kind == Kind.RESERVEDOP)
                && Lexer.contains(ops, currTok.text);
    }

    private Node expression() throws IOException {
        Node e = expr2();
        if (currTok.kind == Kind.QVARSYM && "$".equals(currTok.text)) {
            skip();
            e = appFactory.mkApp(e, expression());
        }
        return e;
    }
    
    private Node expr2() throws IOException {
        List<Node> es = new ArrayList<>();
        es.add(expr3());
        while (currTok.kind == Kind.QVARSYM && "||".equals(currTok.text)) {
            skip();
            es.add(expr3());
        }
        return rassoc(Symbol.valueOf("or"), es);
    }
    
    private Node expr3() throws IOException {
        List<Node> es = new ArrayList<>();
        es.add(expr4());
        while (currTok.kind == Kind.QVARSYM && "&&".equals(currTok.text)) {
            skip();
            es.add(expr4());
        }
        return rassoc(Symbol.valueOf("and"), es);
    }
    
    private Node expr4() throws IOException {
        Node e = expr5();
        if (isOp(REL_OPS)) {
            String op = currTok.text;
            skip();
            e = appFactory.mkApp(Function.valueOf(relopName(op)), e, expr5());
        }
        return e;
    }

    private Node expr5() throws IOException {
        Node e1 = expr6();
        if (currTok.kind == Kind.RESERVEDOP && ":".equals(currTok.text)) {
            skip();
            Node es = expr5();
            return appFactory.mkApp(Function.primPack(1, 2), e1, es);
        }
        if (currTok.kind == Kind.QVARSYM && "++".equals(currTok.text)) {
            skip();
            Node es = expr5();
            return appFactory.mkApp(Symbol.valueOf("append"), e1, es);
        }
        return e1;
    }
    
    private Node rassoc(Node f, List<Node> xs) {
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
            String op = currTok.text.equals("+") ? "add" : "sub";
            skip();
            e = appFactory.mkApp(Function.valueOf(op), e, expr7());
        }
        return e;
    }
    
    private Node expr7() throws IOException {
        Node e = expr9();
        while (isOp(new String[]{"*", "/", "%"})) {
            String op;
            switch (currTok.text) {
            case "*": op = "mul"; break;
            case "/": op = "quot"; break;
            default:  op = "rem"; break;
            }
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
            case "let":
                return let();
            case "letrec":
                return letrec();
            case "Pack":
            case "Get":
                return application();
            default:
                break;
            }
            break;
        default:
            return application();
        }
        syntaxError("one of '\\', 'case', 'if', expression expected");
        return null;
    }

    private Node letrec() throws IOException {
        skip();
        accept(Kind.LBRACE);
        Map<String, Node> ds = defns();
        accept(Kind.RBRACE);
        accept(Kind.RESERVEDID, "in");
        Node body = expression();
        List<String> xs = new ArrayList<>();
        List<Node> es = new ArrayList<>();
        ds.forEach((k,v) -> { xs.add(k); es.add(v); });
        if (xs.size() == 1) {
            // only 1 binding
            Node f = ba.abs(xs.get(0), body);
            Node a = appFactory.mkApp(Function.valueOf("Y"), ba.abs(xs.get(0), es.get(0)));
            return appFactory.mkApp(f, a);
        } else {
            Node f = ba.absMany(xs, appFactory.mkApp(Function.valueOf("K"), body));
            Node es1 = appFactory.mkApp(Function.valueOf("K"), appFactory.mkList(es));
            Node a = appFactory.mkApp(Function.valueOf("Y"), ba.absMany(xs, es1));
            return appFactory.mkApp(f, a);
        }
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

    private Node let() throws IOException {
        skip();
        accept(Kind.LBRACE);
        Map<String, Node> ds = defns();
        accept(Kind.RBRACE);
        accept(Kind.RESERVEDID, "in");
        Node body = expression();
        List<String> xs = new ArrayList<>();
        List<Node> es = new ArrayList<>();
        ds.forEach((k,v) -> { xs.add(k); es.add(v); });
        body = ba.abs(xs, body);
        for (Node e : es) {
            body = appFactory.mkApp(body, e);
        }
        return body;
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
            if (currTok.text.equals("Get")) {
                return getter();
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
            e = Int.valueOf(new BigInteger(currTok.text));
            skip();
            return e;
        case LIT_STRING:
            e = Data.makeString(currTok.text);
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
        skip();
        List<Node> es = new ArrayList<>();
        while (currTok.kind != Kind.RBRACKET) {
            es.add(expression());
            if (currTok.kind == Kind.COMMA) {
                skip();
            }
        }
        skip();
        es.add(Data.valueOf(0));
        return rassoc(Function.primPack(1, 2), es);
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
        return Function.primPack(Integer.parseInt(t), Integer.parseInt(a));
    }

    private Node getter() throws IOException {
        skip();
        accept(Kind.LBRACE);
        String index = currTok.text;
        accept(Kind.LIT_INT);
        accept(Kind.RBRACE);
        return Function.primGet(Integer.parseInt(index));
    }

    private Node conditional() throws IOException {
        skip();
        Node p = expression();
        accept(Kind.RESERVEDID, "then");
        Node c = expression();
        accept(Kind.RESERVEDID, "else");
        Node a = expression();
        return appFactory.mkApp(Function.primCase(new int[]{0,0}, true), a, c, p);
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
            if (currTok.kind == Kind.RBRACE) {
                break;
            }
            if (currTok.kind != Kind.SEMI) {
                syntaxError("';' or '}' expected");
            }
            skip();
        }
        skip();
        
        int[] arities = new int[alts.size()];
        List<Node> altExprs = new ArrayList<>();
        int i = 0;
        boolean hasDefault = false;
        for (Alt a : alts.values()) {
            if (a.tag == Integer.MAX_VALUE) {
                if (hasDefault) {
                    syntaxError("duplicate default case");
                }
                hasDefault = true;
            } else if (a.tag != i) {
                syntaxError("missing tag in case: " + i);
            }
            rTrim(a.xs, "_");
            arities[i] = a.xs.size();
            altExprs.add(ba.abs(a.xs, a.e));
            i++;
        }

        Function c = Function.primCase(arities, hasDefault);
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
            a.tag = Integer.MAX_VALUE;
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
