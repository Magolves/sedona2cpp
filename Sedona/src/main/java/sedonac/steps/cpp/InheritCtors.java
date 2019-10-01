package sedonac.steps.cpp;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.Location;
import sedonac.ast.*;
import sedonac.ir.IrType;
import sedonac.namespace.Method;
import sedonac.namespace.Slot;
import sedonac.namespace.Type;
import sedonac.translate.TranslationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds missing ctors from the base classes
 */
public class InheritCtors extends CompilerStep {

    public InheritCtors(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void run() {
        log.debug("  InheritCtors");

        // then process the AST types
        if (compiler.ast != null)
            walkAst(WALK_TO_TYPES);

        quitIfErrors();
    }

    public void enterType(TypeDef t) {
        super.enterType(t);

        List<MethodDef> constructorDefs = new ArrayList<>();
        collectConstructors(t, constructorDefs);

        if (constructorDefs.size() == 0) {
            return;
        }

        // now we should have a list of unique ctors covering all required ones
        // we ignore the first one (this is ours and is already present)
        for (int i = 1; i < constructorDefs.size(); i++) {
            MethodDef methodDef = constructorDefs.get(i);

            final Location loc = methodDef.loc;

            // Assemble body
            Stmt.ExprStmt exprStmt = new Stmt.ExprStmt(loc, new Expr.Super(loc));
            Block block = new Block(loc);
            //block.stmts.add(exprStmt); // TODO: Call super class here
            block.stmts.add(new Stmt.Return(loc));

            // We append the type of the ctor (NOTE: isInstanceInit required a patch for this)
            final String ctorName = methodDef.name + "_" + methodDef.parent.name;

            ParamDef[] paramDefs = new ParamDef[methodDef.params != null ? methodDef.params.length : 0];
            for (int j = 0; j < methodDef.params.length; j++) {
                paramDefs[j] = methodDef.params[j];
            }

            MethodDef newCtor = new MethodDef(
                    loc, t, methodDef.flags, ctorName, new FacetDef[0], ns.voidType, paramDefs, block);
            t.addSlot(newCtor);
        }
    }

    /**
     * Walks down the hierarchy and collects all (different) constructors
     * @param typeDef the type to chekc for constructors.
     * @param ctorList the liust containg all constructors
     */
    private void collectConstructors(TypeDef typeDef, List<MethodDef> ctorList) {
        for (int i = 0; i < typeDef.methodDefs().length; i++) {
            MethodDef methodDef = typeDef.methodDefs()[i];
            if (methodDef.isInstanceInit()) {

                if (ctorList.isEmpty()) {
                    // first entry -> add w/o check
                    ctorList.add(methodDef);
                } else {
                    MethodDef newCtor = null;

                    // Check, if there is a lready a matching ctor
                    for (int j = 0; j < ctorList.size(); j++) {
                        // check if we found a new ctor signature
                        if (!doSignaturesMatch(ctorList.get(j), methodDef)) {
                            newCtor = methodDef;
                        } else {
                            // ... match found, ignore and stop
                            newCtor = null;
                            break;
                        }
                    }

                    if (newCtor != null) {
                        ctorList.add(newCtor);
                        log.info(String.format("New ctor %s from %s [now %d]", newCtor, typeDef, ctorList.size()));
                    }
                }
            }
        }

        if (typeDef.base instanceof TypeDef) {
            //log.info(String.format("Check base class of '%s'...", typeDef));
            collectConstructors((TypeDef) typeDef.base, ctorList);
        }
    }

    private boolean doSignaturesMatch(Slot as, Slot bs) {
        // fields are never matching overrides
        if (as.isField()) return false;
        if (bs.isField()) return false;

        Method a = (Method) as;
        Method b = (Method) bs;

        // check return types
        if (!a.returnType().equals(b.returnType())) {
            return false;
        }

        // check param count
        Type[] ap = a.paramTypes();
        Type[] bp = b.paramTypes();
        if (ap.length != bp.length) {
            return false;
        }

        // check param types
        for (int i = 0; i < ap.length; ++i) {
            if (!ap[i].equals(bp[i])) {
                return false;
            }
        }

        // must be a match!
        return true;
    }
}
