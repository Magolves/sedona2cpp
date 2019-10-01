package sedonac.steps.cpp;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.Location;
import sedonac.ast.*;
import sedonac.namespace.Slot;
import sedonac.namespace.Type;
import sedonac.parser.Token;
import sedonac.translate.TranslationUtil;

import java.util.ArrayList;
import java.util.List;

import static sedonac.translate.TranslationUtil.*;

public class SortMethodsAndFields extends CompilerStep {
    public SortMethodsAndFields(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void run() {
        log.debug("  SortMethodsAndFields");

        for (int i = 0; i < compiler.ast.types.length; i++) {
            TypeDef type = compiler.ast.types[i];
            type.sort(this::slotCompare);
        }
    }

    public int slotCompare(SlotDef x, SlotDef y) {
        if (x == null) {
            return y != null ? -1 : 0;
        }

        // Don't change the sequence of methods
        if (x.isMethod() && y.isMethod()) {
            return 0;
        }

        int cc = Boolean.compare(x.isField(), y.isField());
        if (cc == 0) {
            cc = Boolean.compare(x.isDefine(), y.isDefine());
            if (cc == 0) {
                cc = Boolean.compare(x.isStatic(), y.isStatic());
                if (cc == 0) {
                    cc = x.name().compareTo(y.name());
                }
            }
        }

        return cc;
    }

}
