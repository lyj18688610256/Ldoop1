package org.clyze.doop.dex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clyze.doop.common.ArtifactScanner;
import org.clyze.doop.common.BasicJavaSupport;
import org.clyze.doop.common.CHA;
import org.clyze.doop.common.Database;
import org.clyze.doop.common.DoopErrorCodeException;
import org.clyze.doop.common.Driver;
import org.clyze.doop.common.android.AndroidSupport;
import org.clyze.utils.JHelper;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.jf.dexlib2.DexFileFactory.loadDexContainer;

public class DexInvoker {

    public static void main(String[] args) throws DoopErrorCodeException {
        start(args, new CHA());
    }

    private static void start(String[] args, CHA cha) throws DoopErrorCodeException {
        DexParameters dexParams = new DexParameters();
        dexParams.initFromArgs(args);

        try {
            JHelper.tryInitLogging("DEBUG", dexParams.getLogDir(), true);
        } catch (IOException ex) {
            System.err.println("WARNING: could not initialize logging");
            throw new DoopErrorCodeException(18, ex, true);
        }

        Log logger = LogFactory.getLog(DexInvoker.class);
        String outDir = dexParams.getOutputDir();
        logger.debug("Using output directory: " + outDir);

        BasicJavaSupport java = new BasicJavaSupport(dexParams, new ArtifactScanner());

        try (Database db = new Database(outDir)) {
            java.preprocessInputs(db);

            DexFactWriter writer = new DexFactWriter(db, dexParams, cha);
            writer.writePreliminaryFacts(java);
            AndroidSupport android = new DexAndroidSupport(dexParams, java);
            try {
                Set<String> tmpDirs = new HashSet<>();
                android.processInputs(tmpDirs);
                System.out.println("Writing components...");
                android.writeComponents(writer);
                android.printCollectedComponents();
                JHelper.cleanUp(tmpDirs);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            for (String apkName : dexParams.getAllInputs()) {
                if (!apkName.endsWith(".apk")) {
                    System.err.println("Input file is not an .apk file: " + apkName);
                    continue;
                }

                File apk = new File(apkName);
                if (!apk.exists())
                    throw new RuntimeException("APK does not exist: " + apkName);

                try {
                    Opcodes opcodes = Opcodes.getDefault();
                    MultiDexContainer<? extends DexBackedDexFile> multiDex = loadDexContainer(apk, opcodes);
                    long time1 = System.currentTimeMillis();
                    for (String dexEntry : multiDex.getDexEntryNames()) {
                        DexBackedDexFile dex = multiDex.getEntry(dexEntry);
                        if (dex != null) {
                            System.out.println("Found dex file '" + dexEntry + "' with " + dex.getClassCount() + " classes in '" + apkName + "'");
                            writer.generateFacts(java, dexParams, apk.getName(), dexEntry, dex);
                        } else
                            throw new RuntimeException("Internal error: null .dex entry for " + dexEntry);
                    }
                    long time2 = System.currentTimeMillis();
                    System.out.println("Dex processing time: " + ((time2 - time1) / 1000.0) + " sec");
                } catch (IOException e) {
                    System.err.println("Error opening APK " + apkName);
                    throw e;
                }
            }

            writer.writeLastFacts(java);
            cha.conclude(db, writer, dexParams._reportPhantoms);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DoopErrorCodeException(17, ex);
        } finally {
            Driver.waitForExecutorShutdown(java.getExecutor());
        }
    }
}
