package de.bokeh.skred.red;

public class RedContext {

    private final AppFactory appFactory;
    
    public RedContext(AppFactory appFactory) {
        this.appFactory = appFactory;
        s = new Node[INITIAL_ALLOC];
        size = INITIAL_ALLOC;
        tos = -1;
        base = 0;
        maxDepth = -1;
    }
    
    public Node mkApp(Node fun, Node arg) {
        return appFactory.newApp(fun, arg);
    }
    
    /**
     * Evaluate TOS to WHNF.
     */
    public void eval() {
        int savedBase = getBase();
        setBaseToTos();
        int chk = getBase();
        getTos().eval(this);
        assert chk == getBase();
        setBase(savedBase);
    }
    
    public Node argCheckFailed() {
        //System.err.println("argcheck failed, tos: " + spine.getTos());
        int n = numArgs();
        pop(n);
        return getTos();
    }

    @Override
    public String toString() {
        return "RedContext{spine=" + getMaxDepth() + "}";
    }

    private static final int INITIAL_ALLOC = 256;

    private void grow() {
        // We just double the size
        int newSize = size * 2;
        //System.err.println("growing spine from " + size + " to " + newSize);
        Node[] tmp = new Node[newSize];
        System.arraycopy(s, 0, tmp, 0, size);
        s = tmp;
        size = newSize;
    }

    private Node[] s;
    private int size;
    private int tos;
    private int base;
    private int maxDepth;

    public Node unwind() {
        Node r;
        do {
            r = s[tos].unwind(this);
        } while (r == null);
        return r;
    }
    
    /**
     * Return the number of function arguments on the stack.
     * That ist the number of items on the stack - 1.
     * @return The number of argments available.
     */
    public int numArgs() {
        return tos - base;
    }

    public Node getArg(int offset) {
        return s[tos - offset].getArg();
    }
    
    public Node getArg1() {
        return s[tos - 1].getArg();
    }
    
    public Node getArg2() {
        return s[tos - 2].getArg();
    }
    
    public Node getArg3() {
        return s[tos - 3].getArg();
    }
    
    /**
     * Get the n'th node.
     * @param offset the node to get. 0 means tos
     * @return The node at offset <code>offset</code>.
     */
    public Node get(int offset) {
        return s[tos - offset];
    }

    public Node get1() {
        return s[tos - 1];
    }
    
    public Node get2() {
        return s[tos - 2];
    }
    
    public Node get3() {
        return s[tos - 3];
    }
    
    public void set(int offset, Node node) {
        s[tos - offset] = node;
    }
    
    public void set1(Node x) {
        s[tos - 1] = x;
    }
    
    public void set2(Node x) {
        s[tos - 2] = x;
    }
    
    public void set3(Node x) {
        s[tos - 3] = x;
    }

    public Node getTos() {
        return s[tos];
    }

    public void setTos(Node Node) {
        s[tos] = Node;
    }

    public void swap() {
        Node tmp = s[tos];
        s[tos] = s[tos-1];
        s[tos-1] = tmp;
    }
    
    /**
     * Pop n topmost nodes.
     * @param n number of nodes to pop
     */
    public void pop(int n) {
        while (n > 0) {
            s[tos] = null;
            tos--;
            n--;
        }
    }

    public void pop1() {
        s[tos] = null;
        tos--;
    }
    
    public void pop2() {
        s[tos-1] = null;
        s[tos] = null;
        tos -= 2;
    }
    
    public void pop3() {
        s[tos-2] = null;
        s[tos-1] = null; 
        s[tos] = null;
        tos -= 3;
    }
    
    /**
     * Push node on stack.
     * @param Node the node to push
     */
    public void push(Node Node) {
        tos++;
        if (tos > maxDepth)
            maxDepth = tos;
        if (tos >= size)
            grow();
        s[tos] = Node;
    }

    public void rearrange(int n) {
        for (int i = 0; i < n; i++) {
            s[tos-i] = s[tos-(i+1)].getArg();
        }
    }
    
    public void rearrange2() {
        s[tos] = s[tos-1].getArg();
        s[tos-1] = s[tos-2].getArg();
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }
    
    public void setBaseToTos() {
        base = tos;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

}
