package de.bokeh.skred.red;

import java.util.*;


abstract public class Function extends Node {

    private final int numArgs;
    private final String name;
    private long evalCount = 0;
    private long unwindCount = 0;
    private long argCheckCount = 0;
    
    protected Function(String name, int numArgs) {
        this.numArgs = numArgs;
        this.name = name;
    }
    
    @Override
    public Node eval(RedContext c) {
        evalCount++;
        return this;
    }

    @Override
    public Node unwind(RedContext c) {
        unwindCount++;
        //System.err.println("unwinding: " + this + ", arity " + this.numArgs + ", " + c.spine.numArgs() + " args on spine");
        if (c.numArgs() < this.numArgs) {
            argCheckCount++;
            return c.argCheckFailed();
        }
        return exec(c);
    }
    
    @Override
    public Node getArg() {
        throw new RedException("getArg of function");
    }

    @Override
    public void overwriteApp(Node f, Node a) {
        throw new RedException("tried to overwrite function");
    }

    @Override
    public void overwriteInd(Node target) {
        throw new RedException("tried to overwrite function");
    }

    @Override
    public void overwriteHole() {
        throw new RedException("tried to overwrite function");
    }

    abstract Node exec(RedContext c);

    public String toString(int maxDepth) {
        return name;
    }
    
    // Factory
    
    private static final int MAX_FUNCTION_CODE = 106;
    
    private static final Function[] functionsByCode = new Function[MAX_FUNCTION_CODE+1];
    private static final Map<String, Function> functionsByName = new HashMap<String, Function>();
    
    private static Function LISTCASE;
    protected static final Function ERROR = new PrimError();
    private static Function I;
    /** I without eval for indirection. */
    public static final Function I_FOR_IND = new CombI();
    private static Function K;
    private static Function K1;
    protected static final Function S = new CombS();
    protected static final Function B = new CombB();
    protected static final Function C = new CombC();
    
    public static void init(boolean evalProjections) {
        if (evalProjections) {
            I = new CombI_Eval();
            K = new CombK_Eval();
            K1 = new CombK1_Eval();
            LISTCASE = new CombListcase_Eval();
            register(48, new PrimCompare0_Eval());
            register(50, new CombIf_Eval());
        } else {
            I = new CombI();
            K = new CombK();
            K1 = new CombK1();
            LISTCASE = new CombListcase();
            register(48, new PrimCompare0());
            register(50, new CombIf());
        }
        
        register(0, S);
        register(1, B);
        register(2, C);
        register(3, new CombS1());
        register(4, new CombB1());
        register(5, new CombC1());
        register(6, new CombBs());
        register(7, I);
        register(8, K);
        register(9, new CombU());
        register(10, new CombY());
        register(11, new CombW());
        register(12, K1);
        register(20, new CombCons());
        register(30, new PrimAddInt());
        register(31, new PrimMulInt());
        register(32, new PrimSubInt());
        register(33, new PrimDivInt());
        register(34, new PrimRemInt());
        register(35, new PrimSucc());
        register(36, new PrimPred());
        register(37, new PrimRsubInt());
        register(38, new PrimRdivInt());
        register(39, new PrimRremInt());
        
        register(40, new PrimLess());
        register(41, new PrimLessEq());
        register(42, new PrimGreater());
        register(43, new PrimGreaterEq());
        register(44, new PrimEq());
        register(45, new PrimNeq());
        register(46, new PrimZero());
        
        register(70, new CombTypePred("boolean"));
        register(71, new CombTypePred("pair"));
        register(73, new CombTypePred("char"));
        register(74, new CombTypePred("number"));
        
        register(100, ERROR);
        register(102, LISTCASE);
        register(105, new PrimRead());
        register(106, new PrimStdPort());
    }
    
    public static Function valueOf(int code) {
        Function f = functionsByCode[code];
        if (f == null)
            throw new IllegalArgumentException("unknown function code: " + code);
        return f;
    }
    
    public static Function valueOf(String name) {
        return functionsByName.get(name);
    }

    private static void register(int code, Function f) {
        assert functionsByCode[code] == null;
        functionsByCode[code] = f;
        Function old = functionsByName.put(f.name, f);
        assert old == null;
    }
    
    public static class Stats {
        public final String name;
        public final long evalCount;
        public final long unwindCount;
        public final long argCheckCount;
        
        public Stats(String name, long eval, long unwind, long argCheck) {
            this.name = name;
            evalCount = eval;
            unwindCount = unwind;
            argCheckCount = argCheck;
        }
    }
    
    private static class CmpStatsUnwindDesc implements Comparator<Stats> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Stats o1, Stats o2) {
            if (o1.unwindCount > o2.unwindCount)
                return -1;
            if (o1.unwindCount < o2.unwindCount)
                return 1;
            if (o1.evalCount > o2.evalCount)
                return -1;
            if (o1.evalCount < o2.evalCount)
                return 1;
            if (o1.argCheckCount > o2.argCheckCount)
                return -1;
            if (o1.argCheckCount > o2.argCheckCount)
                return -1;
            return o1.name.compareTo(o2.name);
        }
        
    }
    
    public static List<Stats> getStats() {
        ArrayList<Stats> st = new ArrayList<Stats>();
        for (int i = 0; i <= MAX_FUNCTION_CODE; i++) {
            Function f = functionsByCode[i];
            if (f != null && f.evalCount + f.unwindCount > 0) {
                st.add(new Stats(f.name, f.evalCount, f.unwindCount, f.argCheckCount));
            }
        }
        Collections.sort(st, new CmpStatsUnwindDesc());
        return st;
    }

    /**
     * @return the i
     */
    protected static Function getI() {
        return I;
    }

    /**
     * @return the k
     */
    protected static Function getK() {
        return K;
    }

    /**
     * @return the k1
     */
    protected static Function getK1() {
        return K1;
    }

    /**
     * @return the lISTCASE
     */
    protected static Function getLISTCASE() {
        return LISTCASE;
    }
    
    public int getArity() {
        return numArgs;
    }
}
