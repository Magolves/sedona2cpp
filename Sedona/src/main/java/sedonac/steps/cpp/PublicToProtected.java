package sedonac.steps.cpp;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.Location;
import sedonac.ast.FieldDef;
import sedonac.namespace.Slot;
import sedonac.translate.TranslationUtil;

public class PublicToProtected extends CompilerStep {
    private int changeCounter = 0;

    public PublicToProtected(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void run() {
        log.debug("  PublicToProtected");
        walkAst(WALK_TO_SLOTS);
        quitIfErrors();
        log.debug("  PublicToProtected [" + changeCounter + "]");
    }

    @Override
    public void enterField(FieldDef f) {
        Location loc = f.loc;
        if (TranslationUtil.isPublicField(f)) {
            // Move field to 'protected' section
            f.flags = (f.flags & ~Slot.PUBLIC) | Slot.PROTECTED;
            changeCounter++;
        }
    }
}
