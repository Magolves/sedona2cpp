/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package test.sedonac;


import org.testng.Assert;
import sedona.Buf;
import sedona.Env;
import sedona.util.ArrayUtil;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.testng.FileAssert.assertDirectory;

/**
 * Helper class to support migration from existing Sedona tests.
 */
@SuppressWarnings("WeakerAccess")
public abstract class SedonaTest {
    int verified;
    int failed;

    //region Verifies
    public void verify(boolean cond, String msg) {
        if (cond) {
            verified++;
        } else {            
            fail(msg);
        }
    }

    public void verify(boolean cond) {
        verify(cond, "failed");
    }

    public void fail() {
        fail("failed");
    }

    public void fail(String msg) {
        Assert.fail(msg);
    }

    public void verifyEq(Object a, Object b) {
        if (a == b)
            verify(true);
        else if (a != null && b != null && a.equals(b))
            verify(true);
        else
            fail(a + " != " + b);
    }

    public void verifyEq(int a, int b) {
        if (a == b)
            verify(true);
        else
            fail(a + " != " + b);
    }

    public void verifyNotEq(int a, int b) {
        if (a != b)
            verify(true);
        else
            fail(a + " == " + b);
    }

    public void verifyEq(long a, long b) {
        if (a == b)
            verify(true);
        else
            fail(a + " != " + b);
    }

    public void verifyEq(float a, float b) {
        if (sedona.Float.equals(a, b))
            verify(true);
        else
            fail(a + " != " + b);
    }

    public void verifyEq(double a, double b) {
        if (sedona.Double.equals(a, b))
            verify(true);
        else
            fail(a + " != " + b);
    }

    public void verifyEq(boolean a, boolean b) {
        if (a == b)
            verify(true);
        else
            fail(a + " != " + b);
    }

    public void verifyEq(byte[] a, byte[] b) {
        if (a == null) {
            if (b == null)
                verify(true);
            else
                fail("null != " + new Buf(b));
        } else if (b == null) {
            fail(new Buf(a) + " != null");
        } else {
            boolean eq = true;
            if (a.length != b.length) eq = false;
            else {
                for (int i = 0; i < a.length; ++i)
                    if (a[i] != b[i]) {
                        eq = false;
                        break;
                    }
            }

            if (eq)
                verify(true);
            else
                fail(new Buf(a) + " != " + new Buf(b));
        }
    }

    public void verifyEq(int[] a, int[] b) {
        if (a == null) {
            if (b == null)
                verify(true);
            else
                fail("null != " + ArrayUtil.toString(b));
        } else if (b == null) {
            fail(ArrayUtil.toString(a) + " != null");
        } else {
            boolean eq = true;
            if (a.length != b.length) eq = false;
            else {
                for (int i = 0; i < a.length; ++i)
                    if (a[i] != b[i]) {
                        eq = false;
                        break;
                    }
            }

            if (eq)
                verify(true);
            else
                fail(ArrayUtil.toString(a) + " != " + ArrayUtil.toString(b));
        }
    }
    //endregion


    public File testDir() {
        File dir = new File(Env.home, "test");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        assertDirectory(dir);
        return dir;
    }

    /**
     * @return the name of svm executable for the current OS.
     */
    public String getSvmName() {
        if (isWindows()) {
            return "svm.exe";
        } else if (isLinux()) {
            return "svm";
        } else throw new IllegalStateException("Unsupported OS: " + System.getProperty("os.name"));
    }

    /**
     * Are we running on a Windows OS?
     */
    public boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") > -1;
    }

    /**
     * Are we running on a Linux OS?
     */
    public boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().indexOf("linux") > -1;
    }

    /**
     * Open a FileWriter instance.
     * Allow for temporary OS conditions that might cause FileWriter cstr to fail.
     */
    @SuppressWarnings("CatchMayIgnoreException")
    public FileWriter openFileWriter(File f) {
        FileWriter fw = null;
        final int maxAttempts = 10;            // max # attempts to open before giving up

        // Retry to avoid test failure due to transient OS issue (file in use, etc).
        // Give it maxAttempts chances to succeed before throwing exception.
        for (int t = 0; t < maxAttempts - 1; t++) {
            try {
                fw = new FileWriter(f);
            } catch (IOException e) {
                Assert.fail("Write failure", e);
            }
            if (fw != null) break;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }

        // Try once more (if necessary), this time catch exception if any & fail
        if (fw == null) try {
            fw = new FileWriter(f);
        } catch (IOException e) {
            e.printStackTrace();
            fail("   Failed to open file " + f + " after " + maxAttempts + " attempts");
        }

        return fw;
    }
}
