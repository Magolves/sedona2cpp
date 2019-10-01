package sedonac.steps;

import sedona.Env;
import sedona.kit.KitDb;
import sedona.kit.KitFile;
import sedona.xml.XElem;
import sedona.xml.XParser;
import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.Location;
import sedonac.ast.KitDef;
import sedonac.ast.NativeDef;
import sedonac.namespace.NativeId;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
public class TranslateResolveNatives extends CompilerStep {
    public TranslateResolveNatives(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void run()
    {
        KitDef kit = compiler.ast;

        try {
            parseNativesFromKitFile(kit);
        } catch (Exception e) {
            try {
                parseNativesFromKitSource(kit);
            } catch(Exception ex) {
                err("Error parsing natives", kit.name, e);
            }
        }
    }

    private void parseNativesFromKitFile(KitDef kit) throws Exception {
        KitFile kitFile = KitDb.matchBest(kit.name);

        // No zipped kit found -> give up
        if (kitFile == null) {
            return;
        }

        try (ZipFile zip = new ZipFile(kitFile.file)) {
            Enumeration e = zip.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                String name = entry.getName();
                if (name.endsWith("kit.xml")) {
                    InputStream in = zip.getInputStream(entry);
                    XElem xml = XParser.make(name, in).parse();
                    parseNativesFromXml(name, xml);
                }
            }
        }

    }

    private void parseNativesFromKitSource(KitDef kit) throws Exception {
        File sedonaHome = Env.home;
        final String sourcePathName = sedonaHome.getAbsolutePath() + File.separator + "src" + File.separator + kit.name;
        File sourceDirectory = new File(sourcePathName);

        String[] kitFiles = sourceDirectory.list((dir, name) -> name.equals("kit.xml"));

        if (kitFiles != null || kitFiles.length == 1) {
            XElem xml = XParser.make(new File(kitFiles[0])).parse();
            parseNativesFromXml(kitFiles[0], xml);
        } else {
            err("No kit.xml found in " + sourceDirectory);
        }
    }

    private void parseNativesFromXml(String sourceName, XElem xml) {
        ArrayList<NativeDef> nativeDefs = new ArrayList<>();
        XElem[] elems = xml.elems("native");

        for (XElem elem : elems) {
            final String qname = elem.get("qname");
            final String id = elem.get("id");
            //final String[] tokens = id.split("(?<kit>\\d+)::(?<id>\\d+)");
            final String[] tokens = id.split("::");
            if (tokens.length == 2) {
                final Location loc = new Location(sourceName);
                final NativeId nativeId = new NativeId(
                        loc,
                        Integer.parseInt(tokens[0]),
                        Integer.parseInt(tokens[1]));
                final NativeDef nativeDef = new NativeDef(loc, qname, nativeId);
                log.debug("Resolved native " + nativeId + "/" + nativeDef);
                nativeDefs.add(nativeDef);
            } else {
                log.warn(String.format("%s: Invalid native address: %s (%s)", sourceName, id, qname));
            }
        }

        compiler.ast.natives = nativeDefs.toArray(new NativeDef[nativeDefs.size()]);
    }
}
