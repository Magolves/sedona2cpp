//
// Copyright (c) 2007 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   29 Mar 07  Brian Frank  Creation
//

package sedonac.steps;

import java.io.*;
import java.util.*;

import sedonac.*;
import sedonac.Compiler;
import sedonac.ast.*;
import sedonac.ir.IrMethod;
import sedonac.namespace.*;

/**
 * ResolveNatives matches the native definitions
 * from kit.xml to the MethodDefs.
 */
public class ResolveNatives
        extends CompilerStep {

    public ResolveNatives(Compiler compiler) {
        super(compiler);
    }

    public void run() {
        KitDef kit = compiler.ast;
        String qnamePrefix = kit.name + "::";
        NativeDef[] natives = kit.natives;
        if (natives == null) natives = new NativeDef[0];

        log.debug("  ResolveNatives [" + natives.length + "]");

        HashMap ids = new HashMap();
        HashMap<String, NativeId> qnames = new HashMap<String, NativeId>();
        boolean[] found = new boolean[natives.length];
        for (int i = 0; i < natives.length; ++i) {
            NativeDef n = natives[i];

            String qname = n.qname;
            NativeId id = n.id;

            // check for duplicates
            if (ids.get(id) != null) {
                err("Duplicate native id '" + id + "'", n.loc);
                found[i] = true;
                continue;
            }
            ids.put(id, n.id);
            qnames.put(qname, n.id);

            // check that qname is for my kit
            if (!qname.startsWith(qnamePrefix)) {
                err("Invalid kit for native method '" + qname + "'", n.loc);
                found[i] = true;
                continue;
            }

            // resolve the method, if null skip (we check later)
            Method m = (Method) ns.resolveMethod(qname);
            if (m == null) continue;

            // check method is native
            if (!m.isNative()) {
                err("Method is not native '" + qname + "'", n.loc);
                found[i] = true;
                continue;
            }

            NativeId nativeId = null;

            if (m instanceof IrMethod) {
                nativeId = ((IrMethod) m).nativeId;
            } else if (m instanceof MethodDef) {
                nativeId = ((MethodDef) m).nativeId;
            } else {
                err("Cannot resolve method '" + qname + "'", n.loc);
            }

            // check method is not already assigned an id
            /*
            if (nativeId != null) {
                err("Native method '" + qname + "' assigned multiple ids", n.loc);
                found[i] = true;
                continue;
            }*/

            // assign native id
            nativeId = id;
            found[i] = true;
        }

        // check we mapped all the ids
        for (int i = 0; i < natives.length; ++i)
            if (!found[i])
                err("Native method not found '" + natives[i].qname + "'", natives[i].loc);

        // check that all the native methods were assigned ids
        for (int i = 0; i < kit.types.length; ++i) {
            MethodDef[] methods = kit.types[i].methodDefs();
            for (int j = 0; j < methods.length; ++j) {
                MethodDef m = methods[j];
                if (m.isNative() && m.nativeId == null) {
                    if (qnames.containsKey(m.qname)) {
                        m.nativeId = qnames.get(m.qname);
                    } else {
                        err("Native method not assigned id '" + m.qname + "'", m.loc);
                    }
                }
            }
        }

    }

}
