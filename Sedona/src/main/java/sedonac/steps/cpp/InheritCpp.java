/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.steps.cpp;

import sedonac.Compiler;
import sedonac.CompilerStep;
import sedonac.ast.SlotDef;
import sedonac.ast.TypeDef;
import sedonac.namespace.Slot;
import sedonac.namespace.Type;
import sedonac.namespace.TypeUtil;

/**
 * InheritCpp replaces Inherit for C++ translation. The problem of Inherit was that all slots of the base classes
 * have been copied into the current class, so all slots - despite overridden or not - reappear in derived classes
 * and blew up the generated code.
 * This class changes this (weird) behaviour by scanning the type hierarchy for the first type which *really* defines
 * this slot.
 * A field defined in a base type will not appear in derived types.
 * A method only, if it is defined/overridden by this particular type
 */
public class InheritCpp extends CompilerStep {
    public InheritCpp(Compiler compiler) {
        super(compiler);
    }

    @Override
    public void run() {
        // then process the AST types
        if (compiler.ast != null)
            walkAst(WALK_TO_TYPES);

        quitIfErrors();
    }

    public void enterType(TypeDef t) {
        super.enterType(t);
        inherit(t);
    }

//////////////////////////////////////////////////////////////////////////
// Inherit
//////////////////////////////////////////////////////////////////////////

    private void inherit(Type t) {
        // if Ob, then nothing to inherit
        if (t.base() == null) return;

        // the types should be ordered by inheritance now, so that
        // means t's base type should already be fully inherited; so
        // now we inherit each of the base type's slots
        Slot[] slots = t.slots();
        for (int i = 0; i < slots.length; ++i) {
            inheritSlot(t, slots[i]);
        }

        // check that everything I thought I was overriding was
        // actually overriding something found in base classes

        if (t instanceof TypeDef) {
            TypeDef def = (TypeDef) t;
            SlotDef[] defs = def.slotDefs();
            for (int i = 0; i < defs.length; ++i)
                if (defs[i].isOverride() && defs[i].overrides == null)
                    err("Override of unknown method '" + defs[i].name + "'", defs[i].loc);
        }
    }

    /**
     * Inherits slots
     * @param t The current type(def)
     * @param slot the slot of the type(def)
     */
    private void inheritSlot(Type t, Slot slot) {
        // we don't inherit special methods like initializers
        if (!slot.isInherited(t)) return;

        // check if we have a SlotDef with the same slotName
        String slotName = slot.name();
        String qualifiedSlotName = slot.qname();
        Slot declared = t.slot(slotName);

        if (!(declared instanceof SlotDef)) return;
        SlotDef slotDef = (SlotDef) declared;

        // set to default (override ourselves)
        Slot overriddenSlot = slot;

        Type baseType = TypeUtil.findSlotInHierarchy(t.base(), slot, slotName);

        if (baseType != null) {
            if (slot.isOverride()) {
                overriddenSlot = baseType.slot(slotName);

                if (slot.isMethod()) {
                    // cannot change from action to non-action, or vice-versa
                    if (slotDef.isAction() != overriddenSlot.isAction()) {
                        err("Inconsistent action tag: '" + slotDef.qname + "' and '" + qualifiedSlotName + "'", slotDef.loc);
                    }

                    // cannot override a non-virtual method
                    if (!slotDef.isVirtual()) {
                        overriddenSlot = slotDef;
                        warn("Override non-virtual method '" + overriddenSlot + "'", slotDef.loc);
                        slotDef.flags = slotDef.flags | Slot.VIRTUAL;
                    }
                }
                slotDef.overrides = overriddenSlot;
            } else {
                // Slot not marked as override -> shadows base slot
                err("Must use 'override' keyword to override '" + qualifiedSlotName + "'", slotDef.loc);
            }
        } else {
            // if slot was marked override, then report mismatch signatures
            if (declared.isOverride()) {
                err("Overridden method '" + slotName + "' does not exist or has a different signature than '" + qualifiedSlotName + "'", slotDef.loc);
            }
            slotDef.overrides = slotDef;
        }

    }

}
