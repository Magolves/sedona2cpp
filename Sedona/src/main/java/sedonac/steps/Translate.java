//
// Copyright (c) 2007 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   15 Mar 07  Brian Frank  Creation
//

package sedonac.steps;

import java.io.*;

import sedonac.*;
import sedonac.Compiler;
import sedonac.ast.*;
import sedonac.namespace.TypeUtil;
import sedonac.translate.*;

/**
 * The Translate step translate Sedona AST into Java or C code.
 */
public class Translate
        extends CompilerStep {

    public Translate(Compiler compiler) {
        super(compiler);
    }

    public void run() {
        Translation t = compiler.translation;

        CppDefaults.setCppOptions(compiler.translation.cppOptions);

        long tStart = System.currentTimeMillis();
        try {
            t.outDir.mkdirs();
            //KitDef kit = t.kits[t.activeKitIndex];
            KitDef kit = compiler.ast;
            log.info("  Translate [" + kit.name + "]");
            log.info("  Translate [" + kit.types.length + " types]");
            // Translate all types
            for (int j = 0; j < kit.types.length; ++j) {
                TypeDef type = kit.types[j];
                translate(type);
            }
            // Translate kit
            translate(kit);

            long tStop = System.currentTimeMillis();
            log.debug(String.format("    [%s] Translation finished [%d ms]", kit.name, (tStop - tStart)));
        } catch (IOException e) {
            e.printStackTrace();
            throw err("Cannot translate", new Location(t.outDir));
        }
    }

    /**
     * Translates the given kit. In general there is nothing to translate here, but
     * this hook is useful to generate a build script or other artifacts with a 'kit' scope.
     *
     * @param kit the kit to 'translate'
     * @throws IOException
     */
    private void translate(KitDef kit) throws IOException {
        Translation t = compiler.translation;
        final String target = t.target;

        if (target.equals("c")) {
            // Nothing to do
        } else if (t.isCpp()) {
            // Makefile
            new CppMakefileTranslator(compiler, kit).translate();
            // Plant UML
            new PlantUmlTranslator(compiler, kit).translate();
        } else {
            throw err("Unknown translation target language '" + target + "'");
        }
    }

    /**
     * Translates a single type. This method is called for all types within a kit
     *
     * @param type the type to translate
     * @throws IOException
     */
    public void translate(TypeDef type)
            throws IOException {

        /* Ignore synthetic types like enums, they are generated with in the header file of the containing type */
        if (type.isEnum()) {
            log.debug("Ignore enum type " + type.qname);
            return;
        }

        Translation t = compiler.translation;

        if (TypeUtil.isaTest(type) && !t.cppOptions.isGenerateTests()) {
            log.debug("Skip test type " + type.qname);
            return;
        }

        final String target = t.target;
        if (target.equals("c")) {
            // translate to C :-(
            new HTranslator(compiler, type).translate();
            new CTranslator(compiler, type).translate();
        } else if (t.isCpp()) {
            // translate to C++ :-)
            if (TypeUtil.isaTest(type)) {
                new GTestHTranslator(compiler, type).translate();
                new GTestTranslator(compiler, type).translate();
            } else {
                new CppHTranslator(compiler, type).translate();
                new CppTranslator(compiler, type).translate();
            }
        } else {
            throw err("Unknown translation target language '" + target + "', try 'c' or 'c++'");
        }
    }

}
