//
// Copyright (c) 2006 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   4 Mar 07  Brian Frank  Creation
//

package sedonac.steps;

import sedona.Env;
import sedona.util.VendorUtil;
import sedona.util.Version;
import sedona.xml.XElem;
import sedona.xml.XException;
import sedona.xml.XParser;
import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.Location;
import sedonac.SourceFile;
import sedonac.ast.DependDef;
import sedonac.ast.IncludeDef;
import sedonac.ast.KitDef;
import sedonac.ast.NativeDef;
import sedonac.namespace.Namespace;
import sedonac.namespace.NativeId;
import sedonac.translate.TranslationUtil;
import sedonac.util.VarResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * InitKitTranslate basically does the same things as {@link InitKitCompile}, but is intended for translation
 * instead of compilation.
 * Translation supports multiple kits while the compiler can only one kit at a time. So this class
 * feeds the translation kit to the compiler and drops the mounted namespace created by the previous run.
 */
public class InitKitTranslate
        extends CompilerStep {

    public InitKitTranslate(Compiler compiler, int kitIndex) {
        super(compiler);

        KitDef translationKit = compiler.translation.kits[kitIndex];
        log.debug("  InitTranslateKit [" + translationKit.name + "]");

        // We fool the compiler by pretending to compile the kit. So we can reuse the compiler steps, but
        // must take care that all instance variables are initialized properly for every kit, since the compiler
        // cannot process multiple kits at once
        final File kitXml = getKitXml(translationKit.name);
        if (kitXml != null) {
            compiler.input = kitXml;
            try {
                compiler.xml = XParser.make(kitXml).parse();
            } catch (Exception e) {
                err("Kit file not found", new Location(kitXml), e);
            }

            // Renew the namespace to drop definitions previous
            compiler.ns = new Namespace();

            // Basic init
            this.kitFile = compiler.input;
            this.kitDir = kitFile.getParentFile();
            this.xml = compiler.xml;
        }
    }

    public void run() {
        try {
            log.debug("  InitKitTranslate");
            parseKitDef();
            findSourceFiles();
            validateNames();
            setupReflectiveTypes();
        } catch (XException e) {
            throw err(e);
        }
    }

    private void setupReflectiveTypes() {
        if (!compiler.translation.cppOptions.isGenerateReflectiveTypes()) {
            for (int i = 0; i < TranslationUtil.REFLECTIVE_TYPES.length; i++) {
                String reflectiveType = TranslationUtil.REFLECTIVE_TYPES[i];
                TranslationUtil.ignoredType(reflectiveType);
                log.info("    Ignore type " + reflectiveType);
            }
        }
    }

    private File getKitXml(String kitName) {
        File sedonaHome = Env.home;
        final String sourcePathName = sedonaHome.getAbsolutePath() + File.separator + "src" + File.separator + kitName + File.separator + "kit.xml";
        File sourcePath = new File(sourcePathName);

        if (sourcePath.exists()) {
            return sourcePath;
        } else {
            return null;
        }
    }

    private void validateNames() {
        KitDef kit = compiler.ast;
        Location loc = new Location(kitFile);
        String name = kit.name;
        String vendor = kit.vendor;

        // if vendor is Tridium we don't enforce kit/vendor name
        // rule since Tridium is the vendor of the core kits
        if (!vendor.equals("Tridium")) {
            try {
                VendorUtil.checkVendorName(vendor);
            } catch (Exception e) {
                throw err(e.getMessage(), loc);
            }

            // we require that the kit name starts with the vendor name
            // to ensure a sane strategy for allowing vendor's to post
            // manifests to the sedonadev.org website
            if (!name.toLowerCase().startsWith(vendor.toLowerCase()))
                throw err("Kit name '" + name + "' must be prefixed with vendor name '" + vendor + "'", loc);
        }

        // kit name can only contain alphanumeric or '_' characters
        name = name.toLowerCase();
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (c >= 'a' && c <= 'z') continue;
            if (c >= '0' && c <= '9') continue;
            if (c == '_') continue;
            throw err("Invalid kit name '" + kit.name + "'. Valid characters: [a-zA-Z0-9_]");
        }
    }

    private void parseKitDef() {
        KitDef kit = new KitDef(new Location(kitFile));

        // root attributes
        kit.name = xml.get("name");
        kit.vendor = xml.get("vendor");
        kit.description = xml.get("description");
        kit.includeSource = xml.getb("includeSource", false);
        kit.doc = xml.getb("doc", false);

        // kit version is defined as follows
        //   1) if command line -kitVersion
        //   2) if defined by kit.xml
        //   3) sedona.properties "buildVersion"
        Version ver = compiler.kitVersion;
        if (ver == null && xml.get("version", null) != null) {
            final String verAttr = xml.get("version");
            try {
                ver = new Version(new VarResolver().resolve(verAttr));
            } catch (IllegalArgumentException e) {
                throw new XException("Invalid attr version = '" + verAttr + "'", xml);
            } catch (Exception e) {
                throw new XException("Could not resolve variable version = '" + verAttr + "'", xml);
            }
        }
        if (ver == null) ver = Version.parse(Env.getProperty("buildVersion", null));
        if (ver == null) throw err("Kit version isn't defined", kit.loc);
        kit.version = ver;

        // depends element
        parseDepends(kit, xml.elems("depend"));

        // includes element
        parseIncludes(kit, xml.elems("include"));

        // natives element
        parseNatives(kit, xml.elems("native"));

        compiler.ast = kit;
    }

    private void parseDepends(KitDef kit, XElem[] xdepends) {
        // depend elements
        kit.depends = new DependDef[xdepends.length];
        for (int i = 0; i < xdepends.length; ++i) {
            XElem x = xdepends[i];
            kit.depends[i] = new DependDef(new Location(x), x.getDepend("on"));
        }

        // check depend on sys
        boolean onSys = kit.name.equals("sys");
        for (int i = 0; i < kit.depends.length; ++i) {
            if (kit.depends[i].depend.name().equals("sys")) {
                onSys = true;
                break;
            }
        }
        if (!onSys) {
            err("Must declare dependency on 'sys'", new Location(xml));
        }
    }

    private void parseIncludes(KitDef kit, XElem[] xincludes) {
        HashSet depends = new HashSet();
        for (int i = 0; i < kit.depends.length; ++i)
            depends.add(kit.depends[i].depend.name());

        // include elements
        HashSet seen = new HashSet();
        kit.includes = new IncludeDef[xincludes.length];
        for (int i = 0; i < xincludes.length; ++i) {
            XElem xinclude = xincludes[i];
            Location xincludeLoc = new Location(xml.location().file, xinclude.line());
            IncludeDef include = kit.includes[i] =
                    new IncludeDef(new Location(xinclude), xinclude.getDepend("from"));

            if (depends.contains(include.depend.name()))
                err("Cannot include and depend on the same kit: '" + include.depend.name() + "'", xincludeLoc);
            if (!seen.add(include.depend))
                err("Duplicate <include> directive for kit: '" + include.depend + "'", xincludeLoc);

            // Store the types that we want to include.
            XElem[] xtypes = xinclude.elems("type");
            for (XElem xtype : xtypes) include.typeToSource.put(xtype.get("name"), null);
        }
    }

    private void parseNatives(KitDef kit, XElem[] xnatives) {
        // native elements
        ArrayList acc = new ArrayList();
        for (int i = 0; i < xnatives.length; ++i) {
            XElem x = xnatives[i];
            String qname = x.get("qname");
            NativeId id = NativeId.parse(new Location(x), x.get("id"));
            if (id == null) throw new XException("Invalid id format", x);
            acc.add(new NativeDef(new Location(x), qname, id));
        }
        kit.natives = (NativeDef[]) acc.toArray(new NativeDef[acc.size()]);
    }

    void findSourceFiles() {
        // find source elements
        XElem[] sources = xml.elems("source");
        if (sources.length == 0)
            throw err("Must specify at least one <source> element", new Location(xml));

        // map each source element to a directory
        ArrayList acc = new ArrayList();
        for (int i = 0; i < sources.length; ++i)
            findSourceFiles(acc, sources[i]);
        compiler.sourceFiles = (SourceFile[]) acc.toArray(new SourceFile[acc.size()]);

        // if verbose dump source files
        if (compiler.log.isDebug()) {
            for (int i = 0; i < compiler.sourceFiles.length; ++i)
                log.debug("    " + compiler.sourceFiles[i]);
        }
    }

    void findSourceFiles(ArrayList acc, XElem xml) {
        String dirName = xml.get("dir");
        File dir = new File(kitFile.getParentFile(), dirName);
        if (!dir.exists() || !dir.isDirectory())
            throw err("Unknown source directory '" + dirName + "'", new Location(xml));

        try {
            // source must be sub-directory of directory kit.xml is in
            if (!dir.getCanonicalPath().startsWith(kitFile.getParentFile().getCanonicalPath()))
                throw err("Source directory must be a sub-directory of " + kitFile.getParentFile(), new Location(xml));
        } catch (IOException e) {
            // silently ignore and fail later
        }

        boolean testOnly = isTestonly(xml);

        File[] list = dir.listFiles();
        for (int i = 0; i < list.length; ++i) {
            File f = list[i];
            if (f.getName().endsWith(".sedona")) {
                try {
                    SourceFile sf = new SourceFile();
                    sf.file = f.getCanonicalFile();
                    sf.testOnly = testOnly;
                    acc.add(sf);
                } catch (IOException e) {
                    throw err("Cannot map to canonical filename: " + f, new Location(xml), e);
                }
            }
        }
    }

    private boolean isTestonly(XElem xsource) {
        Location loc = new Location(xsource);
        boolean testonly = false;
        try {
            testonly = xsource.getb("testonly");
            try {
                // check to see if both test and testOnly were specified.
                boolean deprecatedTestOnly = xsource.getb("test");
                err("Attributes 'test' and 'testonly' cannot both be present. Use 'testonly'.", loc);
            } catch (XException e) {
            }
        } catch (XException e) {
            // see if deprecated 'test' attribute is used instead
            try {
                testonly = xsource.getb("test");
                warn("The 'test' attribute is deprecated. Use 'testonly' instead. ", loc);
            } catch (XException ee) {
            }
        }
        return testonly;
    }

    File kitFile;
    File kitDir;
    XElem xml;

}
