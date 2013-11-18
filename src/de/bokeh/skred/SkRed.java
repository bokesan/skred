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
        List<String> programFiles = new ArrayList<>();
        List<String> cmdArgs = new ArrayList<>();
        boolean evalProjections = false;
        boolean useBStar = true;
        boolean optimize = true;
        boolean prelude = true;
        boolean justCompile = false;
        String appFactoryId = "Cond";
        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            if (s.equals("--app=ST")) {
                appFactoryId = "ST";
            }
            else if (s.equals("--app=Cond")) {
                appFactoryId = "Cond";
            }
            else if (s.equals("--app=IndI")) {
                appFactoryId = "IndI";
            }
            else if (s.equals("--no-prelude")) {
                prelude = false;
            }
            else if (s.equals("--noevalprojections")) {
                evalProjections = false;
            }
            else if (s.equals("--evalprojections")) {
                evalProjections = true;
            }
            else if (s.equals("--useB1")) {
                useBStar = false;
            }
            else if (s.equals("--noopt")) {
                optimize = false;
            }
            else if (s.equals("--compile")) {
                justCompile = true;
            }
            else if (s.startsWith("--stats=")) {
                File statsFile = new File(s.substring(8));
                stats = new PrintWriter(new FileOutputStream(statsFile));
            } else if (s.equals("--")) {
                i++;
                for ( ; i < args.length; i++) {
                    cmdArgs.add(args[i]);
                }
            } else {
                programFiles.add(s);
            }
        }
        AppFactory appFactory;
        switch (appFactoryId) {
        case "IndI": appFactory = new AppIndIFactory(optimize); break;
        case "ST":   appFactory = new AppSTFactory(optimize); break;
        default:     appFactory = new AppCondFactory(optimize); break;
        }
        Function.init(evalProjections);
        SkReader r = new Parser(appFactory, useBStar);
        r.addDefn("cmdLine", makeStringList(cmdArgs, appFactory));
        long startTime = System.nanoTime();
        if (prelude) {
            loadPrelude(r);
        }
        for (String s : programFiles) {
            if (stats != null)
                stats.println("loading " + s);
            BufferedReader in = new BufferedReader(new FileReader(s));
            r.readDefns(in, s);
            in.close();
        }
        double elapsed = (System.nanoTime() - startTime) * 1.0e-9;
        if (stats != null)
            stats.format("load time: %.3f seconds.\n", elapsed);
        if (justCompile) {
            r.dumpDefns("main", System.out);
            return;
        }
        RedContext c = new RedContext(appFactory);
        c.push(appFactory.mkApp(r.getGraph("main"), Int.valueOf(0)));
        r = null;
        startTime = System.nanoTime();
        c.eval();
        if (c.getTos().getTag() != 0) {
            System.err.println("IO error: " + c.getTos());
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

    private static void loadPrelude(SkReader r) throws IOException {
        InputStream s = SkRed.class.getResourceAsStream("/lib/prelude.core");
        if (s == null) {
            s = ClassLoader.getSystemResourceAsStream("prelude.core");
        }
        if (s == null) {
            System.err.println("error: 'prelude.core' not found in classpath.");
            System.exit(1);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(s));
        r.readDefns(in, "prelude");
        in.close();
    }

    private static Node makeStringList(List<String> cmdArgs, AppFactory appFactory) {
        List<Node> args = new ArrayList<>();
        for (String s : cmdArgs) {
            args.add(Data.makeString(s));
        }
        return appFactory.mkList(args);
    }

    private static void printMemoryUsage(PrintWriter w, MemoryUsage m, String type) {
        w.println(type + " initial: " + m.getInit());
        w.println(type + " maximal: " + m.getMax());
        w.println(type + " committed: " + m.getCommitted());
        w.println(type + " used: " + m.getUsed());
    }
}
