/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package test.sedonac;

import sedonac.Compiler;
import sedonac.CompilerException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static org.testng.Assert.fail;

public abstract class CompileTest extends SedonaTest {
    public void compile(String src) {
        File xml = new File("compile-test.xml");
        writeKitXml(xml);
        File source = new File("sedona-test.sedona");
        try {
            FileWriter out = openFileWriter(source);
            out.write(src);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

        try {
            compiler = new CompileTest.TestCompiler();
            compiler.compile(xml);
        } catch (CompilerException e) {
        }

        xml.delete();
        source.delete();
    }

    public void writeKitXml(File xml) {
        try {
            FileWriter out = openFileWriter(xml);
            out.write("<sedonaKit name='sedonacCompileTest' vendor='Tridium' description=''><depend on='sys 1.0+' /><source dir='.' /></sedonaKit>");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test Compiler. Turns off logging, and captures warnings for testing
     * purposes.
     */
    protected class TestCompiler extends Compiler {
        public TestCompiler() {
            super();
            this.log.severity = 1000;
            this.warnings = new ArrayList();
        }

        public void warn(String msg) {
            super.warn(msg);
            warnings.add(msg);
        }

        public ArrayList warnings;
    }

    protected TestCompiler compiler;
    byte[] image;

}
