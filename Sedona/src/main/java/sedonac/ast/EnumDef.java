/*
 * Copyright (c) 2007 Tridium, Inc.
 * Copyright (c) 2019. Oliver Wieland (translation support)
 * Licensed under the Academic Free License version 3.0
 *
 */

package sedonac.ast;


import sedonac.Location;
import sedonac.namespace.Type;
import sedonac.namespace.TypeUtil;

import java.util.Arrays;

/**
 * EnumDef is an artificial field which represents a enum slot.
 */
public class EnumDef extends TypeDef {
    private final String[] tags;
    private final int[] values;

    public EnumDef(Location loc, KitDef kit, int flags, String name, FacetDef[] facets, String[] tags, int[] values) {
        super(loc, kit, flags, name, facets);
        this.tags = tags;

        // No values given -> create standard range
        if (values == null) {
            this.values = new int[tags.length];
            for (int i = 0; i < tags.length; i++) {
                this.values[i] = i;
            }
        } else {
            this.values = values;
        }
    }

    public EnumDef(Location loc, KitDef kit, int flags, String name, FacetDef[] facets, String[] tags) {
        this(loc, kit, flags, name, facets, tags, null);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isRef() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public boolean isArray() {
        return super.isArray();
    }

    @Override
    public boolean is(Type x) {
        return false;
    }

    @Override
    public boolean isBuf() {
        return false;
    }

    @Override
    public boolean isLog() {
        return false;
    }

    @Override
    public boolean isStr() {
        return false;
    }

    @Override
    public boolean isBool() {
        return false;
    }

    @Override
    public boolean isByte() {
        return true;
    }

    @Override
    public boolean isFloat() {
        return false;
    }

    @Override
    public boolean isInt() {
        return false;
    }

    @Override
    public boolean isLong() {
        return false;
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public boolean isShort() {
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public boolean isWide() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return true;
    }

    @Override
    public boolean isReflective() {
        return TypeUtil.isReflective(this);
    }

    public String[] tags() {
        return tags;
    }

    public int[] getValues() {
        return values;
    }

    /**
     * Gets the enum tag for the given ordinal
     * @param ordinal the ordinal value of the enum
     * @return the tag for the ordinal or null, if ordinal is not within the enum range
     */
    public String tag(int ordinal) {
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] == ordinal) {
                    return tags[i];
                }
            }
        }

        return null;
    }

    public int maxValue() {
        return values != null ? Arrays.stream(values).max().getAsInt() : 0;
    }

    public int valueCount() {
        return tags != null ? tags.length : 0;
    }

}
