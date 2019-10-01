package sedonac.translate;

import sedonac.Compiler;
import sedonac.ast.Expr;
import sedonac.ast.KitDef;
import sedonac.ast.TypeDef;
import sedonac.namespace.Type;

import java.io.File;
import java.util.Date;

public class CppStdHeader extends AbstractKitTranslator {

    private boolean written = false;

    public CppStdHeader(Compiler compiler, KitDef kit) {
        super(compiler, kit);
    }

    @Override
    public File toFile() {
        return new File(outDir, "types.h");
    }

    @Override
    public void doTranslate() {
        // This file needs to be written only once
        if (written) return;

        types();

        written = true;
    }

    @Override
    protected String getHeaderText() {
        return "Common type definitions";
    }

    public void types() {
        String barrier = "__TYPES_H__";
        nl();
        w("#ifndef ").w(barrier).nl();
        w("#define ").w(barrier).nl();
        nl();

        w("/* Check for C++11 (2011) */").nl();
        w("#if __cplusplus < 201103L").nl();
        indent++;
        indent().w("#error \"Library requires C++11\"").nl();
        indent--;
        w("#endif").nl().nl();


        w("/* Primitive types (modify to match platform specific types/sizes) */").nl();
        w("typedef unsigned char uint8;").nl();
        w("typedef unsigned short uint16;").nl();

        w("/* Check for compiler long size */").nl();
        w("# if __WORDSIZE == 64").nl();
        w("/* 64bit */").nl();
        w("typedef long int  int64;").nl();
        w("typedef int int32;").nl();
        w("# else").nl();
        w("/* 32bit */").nl();
        w("typedef long int int32;").nl();
        w("typedef long long int int64;").nl();
        w("# endif").nl();

        nl();
        w("#endif").nl();
    }

}
