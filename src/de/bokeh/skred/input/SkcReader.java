package de.bokeh.skred.input;

import de.bokeh.skred.red.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;


/**
 * Read compiled compact SKC code.
 * <p>
 * The file format:
 * TODO
 */
public class SkcReader extends AbstractSkReader {

    private static final byte[] SKC_MAGIC = { 0x59, (byte) 0x84, 0x4c, (byte) 0xee };
    
    private static final int REC_DEFN = 0;
    
    private static final int SKC_PCOMB = 0;
    private static final int SKC_COMB = 1;
    private static final int SKC_APP = 2;
    private static final int SKC_BOOL = 3;
    private static final int SKC_PINT = 4;
    private static final int SKC_INT8 = 5;
    private static final int SKC_INT16 = 6;
    private static final int SKC_INT32 = 7;
    private static final int SKC_CHAR = 8;
    private static final int SKC_PSTR = 9;
    private static final int SKC_STR8 = 10;
    private static final int SKC_STR16 = 11;
    private static final int SKC_STR32 = 12;
    private static final int SKC_PLIST = 13;
    private static final int SKC_LIST8 = 14;
    private static final int SKC_LIST16 = 15;
    private static final int SKC_LIST32 = 16;
    private static final int SKC_PSYM = 17;
    private static final int SKC_SYM8 = 18;
    private static final int SKC_SYM16 = 19;
    private static final int SKC_SYM32 = 20;
    
    /** Unsigned 16-bit checksum. */
    private char checksum;
    
    public SkcReader(AppFactory appFactory) {
        super(appFactory);
    }
    
    public void readDefns(File file) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            readDefns(in);
        }
        finally {
            if (in != null)
                in.close();
        }
 
    }
    
    private void readDefns(InputStream in) throws IOException {
        checkMagic(in);
        for (;;) {
            int c = in.read();
            if (c < 0)
                break;
            checksum = 0;
            if (c == REC_DEFN) {
                Node e = readExpression(in);
                if (!(e instanceof Symbol))
                    throw new SkFileCorruptException("definition: symbol expected");
                String name = ((Symbol)e).toString();
                //System.err.println("reading definition of " + e);
                e = readExpression(in);
                defns.put(name, e);
                //System.err.println(name + " = " + e);
            } else {
                throw new SkFileCorruptException("unknown record: " + c);
            }
            char chk = (char) in.read();
            chk |= (char) in.read() << 8;
            //if (checksum != (char) chk)
            //    throw new SkcFileCorruptException("checksum error: expected " + (int) chk + ", got " + (int) checksum);
        }
    }

    private void checkMagic(InputStream in) throws IOException {
        byte[] magic = new byte[SKC_MAGIC.length];
        if (in.read(magic) != SKC_MAGIC.length || !java.util.Arrays.equals(magic, SKC_MAGIC))
            throw new SkFileCorruptException("not a SKC file - wrong magic number");
    }
    
    private int readByte(InputStream in) throws IOException {
        int val = in.read();
        if (val < 0)
            throw new java.io.EOFException();
        checksum += (char) val;
        return val;
    }
    
    private Symbol readSymbol(int len, InputStream in) throws IOException {
        char[] s = new char[len];
        for (int i = 0; i < len; i++) {
            s[i] = (char) readByte(in);
        }
        return Symbol.valueOf(String.valueOf(s));
    }

    private Data readString(int len, InputStream in) throws IOException {
        if (len == 0) {
            return Data.valueOf(0);
        }
        byte[] s = new byte[len];
        if (in.read(s) < len)
            throw new java.io.EOFException();
        Data r = Data.valueOf(0);
        for (int i = len - 1; i >= 0; i--)
            r = Data.valueOf(1, Int.valueOf(byteToCodePoint(s[i])), r);
        return r;
    }
    
    private static int byteToCodePoint(byte v) {
        if (v < 0)
            return 256 + v;
        return v;
    }
    
    private Node readList(int len, InputStream in) throws IOException {
        if (len == 0)
            return Data.valueOf(0);
        if (len == 1)
            throw new SkFileCorruptException("list of length 1");
        Node[] ns = new Node[len];
        for (int i = 0; i < len; i++)
            ns[i] = readExpression(in);
        Node r = ns[len-1];
        for (int i = len-2; i >= 0; i--)
            r = Data.valueOf(1, ns[i], r);
        return r;
    }

    private Node readApp(InputStream in) throws IOException {
        Node f = readExpression(in);
        Node a = readExpression(in);
        return appFactory.mkApp(f, a);
    }
    
    private Data makeBool(boolean val) {
        return Data.valueOf(val ? 1 : 0);
    }
    
    private Node readExpression(InputStream in) throws IOException {
        int x = readByte(in);
        switch (x & 0x1f) {
        case SKC_PCOMB:         return Function.valueOf(unpack(x));
        case SKC_COMB:          return Function.valueOf(readByte(in));
        case SKC_APP:           return readApp(in);
        case SKC_BOOL:          return makeBool((x & 0x20) != 0);
        case SKC_PINT:          return Int.valueOf(unpackSigned(x));
        case SKC_INT8:          return Int.valueOf(sex8(readByte(in)));
        case SKC_INT16:         x = readByte(in);
                                x |= readByte(in) << 8;
                                return Int.valueOf(sex16(x));
        case SKC_INT32:         x = readByte(in);
                                x |= readByte(in) << 8;
                                x |= readByte(in) << 16;
                                x |= readByte(in) << 24;
                                return Int.valueOf(x);
        case SKC_CHAR:          return Int.valueOf(readByte(in));
        case SKC_PSTR:          return readString(unpack(x), in);
        case SKC_STR8:          return readString(readByte(in), in);
        case SKC_PLIST:         return readList(unpack(x), in);
        case SKC_LIST8:         return readList(readByte(in), in);
        case SKC_PSYM:          return readSymbol(unpack(x) + 1, in);
        case SKC_SYM8:          return readSymbol(readByte(in) + 1, in);
        default:
            throw new SkFileCorruptException("unexpected byte: " + x);
        }
    }

    private static int unpack(int x) {
        return x >> 5;
    }
    
    private static int unpackSigned(int x) {
        x >>= 5;
        if (x >= 4)
            return -x;
        return x;
    }
    
    private static int sex8(int n) {
        if (n < 128)
            return n;
        return n - 256;
    }
    
    private static int sex16(int n) {
        if (n < 32768)
            return n;
        return n - 65536;
    }
}
