package de.bokeh.skred;

import java.io.*;
import java.lang.management.*;
import java.util.*;

import de.bokeh.skred.core.Parser;
import de.bokeh.skred.input.*;
import de.bokeh.skred.red.*;


/**
 * Main class.
 */
public class SkRed {

    public static void main(String[] args) throws IOException {
        PrintWriter stats = null;
        List<String> programFiles = new ArrayList<String>();
        AppFactory appFactory = new AppCondFactory();
        boolean evalProjections = true;
        for (String s : args) {
            if (s.equals("--app=ST")) {
                appFactory = new AppSTFactory();
            }
            else if (s.equals("--app=Cond")) {
                appFactory = new AppCondFactory();
            }
            else if (s.equals("--app=IndI")) {
                appFactory = new AppIndIFactory();
            }
            else if (s.equals("--noevalprojections")) {
                evalProjections = false;
            }
            else if (s.equals("--evalprojections")) {
                evalProjections = true;
            }
            else if (s.startsWith("--stats=")) {
                File statsFile = new File(s.substring(8));
                stats = new PrintWriter(new FileOutputStream(statsFile));
            } else {
                programFiles.add(s);
            }
        }
        Function.init(evalProjections);
        SkReader r = new Parser(appFactory);
        long startTime = System.nanoTime();
        for (String s : programFiles) {
            if (stats != null)
                stats.println("loading " + s);
            r.readDefns(new File(s));
        }
        double elapsed = (System.nanoTime() - startTime) * 1.0e-9;
        if (stats != null)
            stats.format("load time: %.3f seconds.\n", elapsed);
        RedContext c = new RedContext(appFactory);
        c.push(r.getGraph());
        r = null;
        startTime = System.nanoTime();
        for (;;) {
            c.eval();
            Node x = c.getTos();
            if (!(x instanceof Data))
                break;
            Data p = (Data) x;
            if (p.getTag() != 1 || p.getNumFields() != 2) {
                if (p.getTag() != 0 || p.getNumFields() != 0) {
                    System.out.println("unexpected result: " + p.toString(5));
                }
                break;
            }
            c.push(p.getField(0));
            c.eval();
            Node ch = c.getTos();
            int char_ = ch.intValue();
            if (char_ <= 0) {
                System.out.println("invalid char: " + char_);
            } else {
                System.out.print((char) char_);
            }
            c.pop1();
            c.setTos(p.getField(1));
        }
        if (stats != null) {
            elapsed = (System.nanoTime() - startTime) * 1.0e-9;
            stats.println("skred version " + Version.getLongVersionString());
            stats.println("evaluate projection combinators: " + evalProjections);
            stats.println("overwriting: " + appFactory);
            
            StringBuilder b = new StringBuilder("Systeminformationen:");
            {
                Runtime ru = Runtime.getRuntime();
                b.append("\nSpeicher: ");
                b.append(ru.totalMemory());
                b.append(" total, ");
                b.append(ru.maxMemory());
                b.append(" max, ");
                b.append(ru.freeMemory());
                b.append(" free");
                b.append("\nAnzahl Prozessoren: ");
                b.append(ru.availableProcessors());
                //stats.println(b.toString());
            }

            b = new StringBuilder("System-Properties:");
            for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
                b.append('\n');
                b.append(e.getKey());
                b.append('=');
                b.append(e.getValue());
            }
            //stats.println(b.toString());

            stats.println(c.toString());
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            stats.println("OS: " + os.getName());
            stats.println("OS Version: " + os.getVersion());
            stats.println("Arch: " + os.getArch());
            stats.println("Load Average: " + os.getSystemLoadAverage());
            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            long totalGCTime = 0;
            for (GarbageCollectorMXBean gb : ManagementFactory.getGarbageCollectorMXBeans()) {
                totalGCTime += gb.getCollectionTime();
                stats.print("GC " + gb.getName() + ": " + gb.getCollectionCount() + " GCs, " + gb.getCollectionTime() + " ms.");
                stats.println(" " + Arrays.deepToString(gb.getMemoryPoolNames()));
            }
            stats.format("%.3f sec. in GC (%.1f%%)\n", totalGCTime / 1000.0, (totalGCTime / 10.0) / elapsed);
            printMemoryUsage(stats, mem.getHeapMemoryUsage(), "heap");
            printMemoryUsage(stats, mem.getNonHeapMemoryUsage(), "non-heap");
            stats.println("Combinator         unwind   argcheck       eval");
            long totalUnwind = 0;
            long totalArgCheck = 0;
            long totalEval = 0;
            for (Function.Stats s : Function.getStats()) {
                totalUnwind += s.unwindCount;
                totalArgCheck += s.argCheckCount;
                totalEval += s.evalCount;
                stats.format("%-12s %12d %10d %10d\n", s.name, s.unwindCount, s.argCheckCount, s.evalCount);
            }
            stats.format("%-12s %12d %10d %10d\n", "=total=", totalUnwind, totalArgCheck, totalEval);
            long numReductions = totalUnwind - totalArgCheck;
            stats.format("elapsed: %.3f sec., %.3f Mrps\n", elapsed, 1.0e-6 * numReductions / elapsed);
            stats.close();
        }
    }

    private static void printMemoryUsage(PrintWriter w, MemoryUsage m, String type) {
        w.println(type + " initial: " + m.getInit());
        w.println(type + " maximal: " + m.getMax());
        w.println(type + " committed: " + m.getCommitted());
        w.println(type + " used: " + m.getUsed());
    }
}
