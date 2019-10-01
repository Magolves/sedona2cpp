//
// Copyright (c) 2006 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   15 Mar 07  Brian Frank  Creation
//

package sedonac.steps;

import sedona.Env;
import sedona.kit.KitDb;
import sedona.kit.KitFile;
import sedona.util.Version;
import sedona.xml.XElem;
import sedona.xml.XException;
import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.Location;
import sedonac.ast.KitDef;
import sedonac.translate.CppDefaults;
import sedonac.translate.Translation;
import sedonac.translate.TranslationUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static sedonac.translate.CppDefaults.*;

/**
 * InitTranslate parses the XML file to get the meta-data and kit
 * list for translation into Java/C.
 */
@SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument", "unchecked", "WeakerAccess"})
public class InitTranslate
        extends CompilerStep {

//////////////////////////////////////////////////////////////////////////
// Run
//////////////////////////////////////////////////////////////////////////

    public InitTranslate(Compiler compiler) {
        super(compiler);
        this.xmlFile = compiler.input;
        this.xmlDir = xmlFile.getParentFile();
        this.xml = compiler.xml;
        compiler.doc = true;
    }

    public void run() {
        try {
            log.debug("  InitTranslate");
            parseTranslation();
            parseKitsToTranslate();

        } catch (XException e) {
            throw err(e);
        }
    }

//////////////////////////////////////////////////////////////////////////
// XML
//////////////////////////////////////////////////////////////////////////

    private void parseTranslation() {
        Translation t = new Translation();
        t.main = xml.get("main");
        t.target = xml.get("target");
        t.outDir = new File(xmlDir, xml.get("outDir", t.target));

        XElem cppOptions = xml.elem("cpp");
        if (cppOptions != null) {
            t.cppOptions.setHeaderExt(cppOptions.get("hdrExt", DEFAULT_SOURCE_EXTENSION));
            t.cppOptions.setSourceExt(cppOptions.get("srcExt", DEFAULT_HEADER_EXTENSION));
            t.cppOptions.setObjectExt(cppOptions.get("objExt", DEFAULT_OBJECT_EXTENSION));
            t.cppOptions.setUsingStd("true".equalsIgnoreCase(cppOptions.get("usingStd", "")));
            t.cppOptions.setDisableErrorCheck("true".equalsIgnoreCase(cppOptions.get("disableErrorCheck", "")));
            t.cppOptions.setMemberPrefix(cppOptions.get("memberPrefix", ""));
            t.cppOptions.setMemberPostfix(cppOptions.get("memberPostfix", ""));
            t.cppOptions.setGenerateMethodImpl("true".equalsIgnoreCase(cppOptions.get("generateMethodImpl", "")));
            t.cppOptions.setGenerateTests("true".equalsIgnoreCase(cppOptions.get("generateTests", "")));
            t.cppOptions.setGenerateReflectiveTypes("true".equalsIgnoreCase(cppOptions.get("generateReflectiveTypes", "")));
            log.info("    " + t.cppOptions.toString());
        } else {
            log.info("No C++ options found, using defaults: " + t.cppOptions);
        }
        CppDefaults.setCppOptions(t.cppOptions);
        compiler.translation = t;
    }

//////////////////////////////////////////////////////////////////////////
// Parse Kits
//////////////////////////////////////////////////////////////////////////

    /**
     * Collects all kits for translation -> compiler.translation.kits
     */
    private void parseKitsToTranslate() {
        XElem[] elems = xml.elems("kit");
        if (elems.length == 0)
            throw err("Must specify at least one <kit> element", new Location(xml));

        List<KitDef> kitDefs = new ArrayList<>();
        for (XElem elem : elems) {
            String name = elem.get("name");
            try {
                KitDef kitDef = verifyKitSource(name);
                if (kitDef == null) {
                    kitDef = verifyKitBinary(name);
                }

                log.info(String.format("    [%s] Source found in %s", name, kitDef.loc));
                kitDefs.add(kitDef);

            } catch (Exception e) {
                throw err("Cannot parse kit: " + name, (Location) null, e);
            }
        }


        // kit version is defined as follows
        //   1) if command line -kitVersion
        //   2) if defined by kit.xml
        //   3) sedona.properties "buildVersion"
        Version ver = compiler.kitVersion;

        compiler.translation.kits = kitDefs.toArray(new KitDef[kitDefs.size()]);
    }

    /**
     * Checks the given directory for a valid kit.
     *
     * @param kitName the name of the kit.
     * @return the kit def instance or null, if directory does not contain a (valid) kit
     */
    private KitDef verifyKitSource(String kitName) throws Exception {
        File sedonaHome = Env.home;
        final String sourcePathName = sedonaHome.getAbsolutePath() + File.separator + "src" + File.separator + kitName;
        File sourceDirectory = new File(sourcePathName);

        String[] sedonaFiles = sourceDirectory.list((dir, name) -> name.endsWith(".sedona"));

        if (sedonaFiles == null || sedonaFiles.length == 0) {
            err("No source files found in " + sourceDirectory);
        }

        // init KitDef
        KitDef kit = new KitDef(new Location(sourceDirectory));
        kit.name = kitName;

        return kit;
    }

    /**
     * Checks for a binary kit
     * @param kitName the name of the kit
     * @throws IOException kit files could not be found or read
     */
    private KitDef verifyKitBinary(String kitName)
            throws IOException {

        KitFile kitFile = KitDb.matchBest(kitName);
        if (kitFile == null) {
            throw err("Cannot find kit '" + kitName + "'", new Location(kitName));
        }

        log.debug(String.format("   [%s -> %s]", kitName, kitFile));

        // init KitDef
        KitDef kit = new KitDef(new Location(kitFile.file));
        kit.name = kitName;

        return kit;
    }


//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

    File xmlDir;
    File xmlFile;
    XElem xml;

}
