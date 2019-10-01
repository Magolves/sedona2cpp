/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.translate;

import sedonac.Compiler;
import sedonac.ast.KitDef;
import sedonac.ast.TypeDef;
import sedonac.namespace.Type;

import java.io.File;
import java.util.Date;

import static sedonac.translate.CppDefaults.*;
import static sedonac.translate.TranslationUtil.findDependentTypes;
import static sedonac.translate.TranslationUtil.isCppType;

@SuppressWarnings("SpellCheckingInspection")
public class CppMakefileTranslator extends AbstractCppKitTranslator {

    private static final String SRCS_PREFEIX = "SRCS = ";
    private static final String OBJS_PREFIX = "OBJS = ";

    public CppMakefileTranslator(Compiler compiler, KitDef kit) {
        super(compiler, kit);
    }

    @Override
    public File toFile() {
        File dir = new File(outDir + File.separator + kit.name);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return new File(dir, "Makefile");
    }


    @Override
    public void doTranslate() {
        StringBuilder srcs = new StringBuilder(SRCS_PREFEIX);
        StringBuilder objs = new StringBuilder(OBJS_PREFIX);
        final String sourceSpace = makeSpaces(SRCS_PREFEIX.length());
        final String objSpace = makeSpaces(OBJS_PREFIX.length());

        boolean insertComma = false;
        for (int i = 0; i < kit.types.length; i++) {
            TypeDef type = kit.types[i];

            if (!acceptType(type)) continue;

            if (insertComma) {
                srcs.append(" \\").append(System.lineSeparator());
                srcs.append(sourceSpace);

                objs.append(" \\").append(System.lineSeparator());
                objs.append(objSpace);
            }
            srcs.append(type.name).append(".").append(getSourceExtension());
            objs.append(type.name).append(".").append(getObjectFileExtension());
            insertComma = true;
        }

        w(srcs.toString()).nl().nl();
        w(objs.toString()).nl().nl();

        w("INCLUDES = -I. -I..").nl();

        // TODO: Move to config file
        w("CPPFLAGS = -std=c++11 -Wall -Wextra -Wshadow -Wnon-virtual-dtor -pedantic").nl();

        w("# The library to build").nl();
        w(String.format("MAIN = lib%s.%s", kit.name, CPP_STATIC_LIB_EXTENSION)).nl().nl();

        w("all: $(MAIN)").nl().nl();

        // NOTE: make requires tabs to recognize rules correctly, so we cannot use indent() here
        w("$(MAIN): $(OBJS)").nl();
        w("\tar -rs $(MAIN) $(OBJS) $(LIBS)").nl();
        w("\tranlib $(MAIN)").nl();
        w(String.format(".%s.%s:", getSourceExtension(), getObjectFileExtension())).nl();
        w("\t$(CXX) $(CPPFLAGS) $(INCLUDES) -c $< -o $@").nl().nl();

        w("clean:").nl();
        w("\trm $(MAIN) $(OBJS)").nl().nl();

        w("# Dependencies").nl();
        StringBuilder deps = new StringBuilder();
        for (int i = 0; i < kit.types.length; i++) {
            TypeDef type = kit.types[i];

            if (!acceptType(type)) continue;

            deps.append(type.name).
                    append(".").
                    append(getObjectFileExtension()).
                    append(": ");


            deps.append(type.name).
                    append(".").
                    append(getHeaderExtension());

            Type[] includes = findDependentTypes(type, t -> !isCppType(t.qname()), false);
            for (int j = 0; j < includes.length; j++) {
                Type dependentType = includes[j];
                deps.append(" ");

                if (!kit.name.equals(dependentType.kit().name())) {
                    deps.append("../").append(dependentType.kit().name()).append("/");
                }
                deps.append(dependentType.name()).
                        append(".").
                        append(getHeaderExtension());
            }
            w(deps.toString()).nl();
            deps.delete(0, deps.length());
        }

    }

    @Override
    public void header(String purpose) {
        w("##########################################################").nl();
        w("# " + purpose).nl();
        w("# " + COPY_RIGHT).nl();
        w("# Tag      : $Id$").nl();
        w("# Namespace: " + kit.name).nl();
        w("# Generated: " + new Date()).nl();
        w("##########################################################/").nl().nl();
    }

    @Override
    protected String getHeaderText() {
        return "Makefile for kit '" + kit.name + "'";
    }

    private String makeSpaces(int length) {
        StringBuilder outputBuffer = new StringBuilder(length);
        for (int i = 0; i < length; i++){
            outputBuffer.append(" ");
        }
        return outputBuffer.toString();
    }
}
