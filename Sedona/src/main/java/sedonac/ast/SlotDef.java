//
// Copyright (c) 2007 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   12 Feb 07  Brian Frank  Creation
//

package sedonac.ast;

import sedona.Facets;
import sedonac.*;
import sedonac.namespace.*;

/**
 * SlotDef
 */
public abstract class SlotDef
  extends FacetsNode
  implements Slot
{

//////////////////////////////////////////////////////////////////////////
// Constructor
//////////////////////////////////////////////////////////////////////////

  public SlotDef(Location loc, TypeDef parent, int flags, String name, FacetDef[] facets)
  {
    super(loc, facets);
    this.parent  = parent;
    this.flags   = flags;
    this.name    = name;
    this.qname   = parent.qname + "." + name;
    this.rtFlags = -1;
  }                               

//////////////////////////////////////////////////////////////////////////
// Slot
//////////////////////////////////////////////////////////////////////////

  public Type parent()  { return parent; }
  public String name()  { return name; }
  public String qname() { return qname; }

  public boolean isInherited(Type into) { return TypeUtil.isInherited(this, into); }
  public boolean isReflective() { return isAction() || isProperty(); }

  public int flags() { return flags; }
  public boolean isAbstract()  { return (flags & ABSTRACT)  != 0; }
  public boolean isAction()    { return (flags & ACTION)    != 0; }
  public boolean isConst()     { return (flags & CONST)     != 0; }
  public boolean isDefine()    { return (flags & DEFINE)    != 0; }

  public boolean isInline()    { return (flags & INLINE)    != 0; }
  public boolean isInternal()  { return (flags & INTERNAL)  != 0; }
  public boolean isNative()    { return (flags & NATIVE)    != 0; }
  public boolean isOverride()  { return (flags & OVERRIDE)  != 0; }

  public boolean isPrivate()   { return (flags & PRIVATE)   != 0; }
  public boolean isProperty()  { return (flags & PROPERTY)  != 0; }
  public boolean isProtected() { return (flags & PROTECTED) != 0; }
  public boolean isPublic()    { return (flags & PUBLIC)    != 0; }

  public boolean isStatic()    { return (flags & STATIC)    != 0; }
  public boolean isVirtual()   { return (flags & VIRTUAL)   != 0; }

  public int rtFlags() 
  { 
    if (rtFlags == -1) throw new IllegalStateException("rtFlags not resolved yet");
    return rtFlags; 
  }
  public boolean isRtAction() { return (rtFlags() & RT_ACTION) != 0; }
  public boolean isRtConfig() { return (rtFlags() & RT_CONFIG) != 0; }
  public void setRtFlags(int rtFlags) { this.rtFlags = rtFlags; }

//////////////////////////////////////////////////////////////////////////
// AstNode
//////////////////////////////////////////////////////////////////////////

  public abstract void walk(AstVisitor visitor, int depth);

//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

  public final TypeDef parent;
  public final String name;
  public final String qname;
  // ow, 23.09.19: Removed 'final' modifier to change scope of fields (getter/setter)
  public int flags;
  private int rtFlags;
  public boolean synthetic;
  public String doc;
  public int declaredId = -1;   // if reflective
  public Slot overrides;        // used in Inherit step

}
