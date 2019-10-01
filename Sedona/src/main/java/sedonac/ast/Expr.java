//
// Copyright (c) 2007 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   12 Feb 07  Brian Frank  Creation
//

package sedonac.ast;

import sedona.Double;
import sedona.Float;
import sedona.Long;
import sedona.*;
import sedonac.Location;
import sedonac.namespace.Slot;
import sedonac.namespace.Type;
import sedonac.namespace.*;
import sedonac.parser.Token;

import java.util.ArrayList;

/**
 * Expr
 */

public abstract class Expr
  extends AstNode
{

//////////////////////////////////////////////////////////////////////////
// Ids
//////////////////////////////////////////////////////////////////////////

  public static final int TRUE_LITERAL   = 1;  // Literal
  public static final int FALSE_LITERAL  = 2;
  public static final int INT_LITERAL    = 3;
  public static final int LONG_LITERAL   = 4;
  public static final int FLOAT_LITERAL  = 5;
  public static final int DOUBLE_LITERAL = 6;
  public static final int TIME_LITERAL   = 7;
  public static final int NULL_LITERAL   = 8;
  public static final int STR_LITERAL    = 9;
  public static final int BUF_LITERAL    = 10;
  public static final int TYPE_LITERAL   = 11;
  public static final int SLOT_LITERAL   = 12;
  public static final int ARRAY_LITERAL  = 13;
  public static final int SIZE_OF        = 14;  
  public static final int SLOT_ID_LITERAL= 15;  
  public static final int NEGATE         = 20;  // Unary
  public static final int COND_NOT       = 21;
  public static final int BIT_NOT        = 22;
  public static final int PRE_INCR       = 23;
  public static final int PRE_DECR       = 24;
  public static final int POST_INCR      = 25;
  public static final int POST_DECR      = 26;
  public static final int COND_OR        = 27;  // Cond
  public static final int COND_AND       = 28;
  public static final int EQ             = 29;  // Binary
  public static final int NOT_EQ         = 30;
  public static final int GT             = 31;
  public static final int GT_EQ          = 32;
  public static final int LT             = 33;
  public static final int LT_EQ          = 34;
  public static final int BIT_OR         = 35;
  public static final int BIT_XOR        = 36;
  public static final int BIT_AND        = 37;
  public static final int LSHIFT         = 38;
  public static final int RSHIFT         = 39;
  public static final int MUL            = 40;
  public static final int DIV            = 41;
  public static final int MOD            = 42;
  public static final int ADD            = 43;
  public static final int SUB            = 44;
  public static final int ASSIGN         = 45;
  public static final int ASSIGN_ADD     = 46;
  public static final int ASSIGN_SUB     = 47;
  public static final int ASSIGN_MUL     = 48;
  public static final int ASSIGN_DIV     = 49;
  public static final int ASSIGN_MOD     = 50;
  public static final int ASSIGN_BIT_OR  = 51;
  public static final int ASSIGN_BIT_XOR = 52;
  public static final int ASSIGN_BIT_AND = 53;
  public static final int ASSIGN_LSHIFT  = 54;
  public static final int ASSIGN_RSHIFT  = 55;
  public static final int ELVIS          = 56;
  public static final int TERNARY        = 57;  // Ternary
  public static final int NAME           = 58;  // Name
  public static final int PARAM          = 59;  // Param
  public static final int LOCAL          = 60;  // Local
  public static final int THIS           = 61;  // This
  public static final int SUPER          = 62;  // Super
  public static final int FIELD          = 63;  // Field
  public static final int INDEX          = 64;  // Index
  public static final int CALL           = 65;  // Call
  public static final int CAST           = 66;  // Cast
  public static final int INIT_ARRAY     = 67;  // InitArray
  public static final int INIT_VIRT      = 68;  // InitVirt
  public static final int INIT_COMP      = 69;  // InitComp
  public static final int STATIC_TYPE    = 70;  // StaticType
  public static final int PROP_SET       = 71;  // Call
  public static final int INTERPOLATION  = 72;  // Interpolation
  public static final int NEW            = 73;  // New
  public static final int DELETE         = 74;  // Delete
  public static final int PROP_ASSIGN    = 75;  // Property assignment :=

  public static final int ENUM_LITERAL   = 100;


//////////////////////////////////////////////////////////////////////////
// Expr
//////////////////////////////////////////////////////////////////////////

  Expr(Location loc, int id)
  {
    super(loc);
    this.id = id;
  }

  public final Expr walk(AstVisitor visitor)
  {
    if (type != null) type = visitor.type(type);
    doWalk(visitor);
    return visitor.expr(this);
  }

  public Stmt toStmt()
  {
    return new Stmt.ExprStmt(loc, this);
  }
  
  public boolean isAssign()
  {                
    return false;
  }                     
  
  public boolean isIncrDecr()
  {
    return false;
  }
  
  public boolean isStmt()
  {
    switch (id)
    {
      case PRE_INCR:
      case PRE_DECR:
      case POST_INCR:
      case POST_DECR:
      case ASSIGN:
      case ASSIGN_ADD:
      case ASSIGN_SUB:
      case ASSIGN_MUL:
      case ASSIGN_DIV:
      case ASSIGN_MOD:
      case ASSIGN_BIT_AND:
      case ASSIGN_BIT_OR:
      case ASSIGN_BIT_XOR:
      case ASSIGN_LSHIFT:
      case ASSIGN_RSHIFT:
      case PROP_ASSIGN:
      case CALL:
      case INIT_ARRAY:
      case INIT_VIRT:
      case INIT_COMP:
      case DELETE:
        return true;
      default:
        return false;
    }
  }

  public boolean isLiteral() { return false; }

  public Expr.Literal toLiteral() { return null; }

  public boolean isDefine() { return false; }

  public String  toCodeString() { throw new IllegalStateException("not literal: " + getClass().getName()); }

  public boolean isAssignable() { return false; }
  
  public Integer toIntLiteral() { return null; }

  public boolean isConst() { return isLiteral(); } // ow, 26.09.19

  protected abstract void doWalk(AstVisitor visitor);

  public void write(AstWriter out)
  {
    out.w(this);
  }

  public abstract String toString();

  /**
   * Return true if this is a null literal which can be
   * coerced into the specific type (ref, bool, or float).  
   * Return false if not a null literal or if the specific 
   * type cannot be coerced into null.
   */
  public boolean isNullLiteral(Type t)
  {                           
    if (id == NULL_LITERAL && t.isNullable())
    {
      this.type = t;   
      return true;
    }
    return false;
  }                                              
  
  /**
   * Return the max number of items this expression 
   * pushes onto the stack.
   */
  public abstract int maxStack();
                                                                              
//////////////////////////////////////////////////////////////////////////
// Literal
//////////////////////////////////////////////////////////////////////////

  @SuppressWarnings({"UnnecessaryUnboxing", "UnnecessaryBoxing", "RedundantCast", "DuplicateBranchesInSwitch"})
  public static class Literal extends Expr
  {
    public Literal(Location loc, Namespace ns, int id, Object value)
    {
      super(loc, id);
      this.value = value;
      switch (id)
      {
        case TRUE_LITERAL:       type = ns.boolType;   break;
        case FALSE_LITERAL:      type = ns.boolType;   break;
        case INT_LITERAL:        type = ns.intType;    break;
        case LONG_LITERAL:       type = ns.longType;   break;
        case FLOAT_LITERAL:      type = ns.floatType;  break;
        case DOUBLE_LITERAL:     type = ns.doubleType; break;
        case TIME_LITERAL:       type = ns.longType;   break;
        case STR_LITERAL:        type = ns.strType;    break;
        case BUF_LITERAL:        type = ns.bufType;    break;
        case TYPE_LITERAL:       type = ns.typeType;   break;
        case SLOT_LITERAL:       type = ns.slotType;   break;
        case NULL_LITERAL:       type = ns.objType;    break;
        case SIZE_OF:            type = ns.intType;    break;
        case ENUM_LITERAL:       type = ns.intType;    break;
        default: throw new IllegalStateException();
      }
    }

    public Literal(Location loc, int id, Type type, Object value)
    {
      super(loc, id);
      this.type  = type;
      this.id    = id;
      // Enum: EnumObject
      this.value = value;
    }

    public boolean isLiteral() { return true; }

    public Expr.Literal toLiteral() { return this; }

    public boolean isZero() 
    {
      switch (id)            
      {
        case INT_LITERAL:    return asInt() == 0;
        case LONG_LITERAL:   return asLong() == 0L;
        case FLOAT_LITERAL:  return asFloat() == 0f;
        case DOUBLE_LITERAL: return asDouble() == 0d;
        case TIME_LITERAL:   return asLong() == 0L;
        case ENUM_LITERAL:   return asInt() == 0;
        default:             return false;
      }
    }
    
    public Integer toIntLiteral() 
    { 
      if (id == INT_LITERAL) {
        return (Integer)value;
      } else if (id == ENUM_LITERAL) {
        EnumObject enumObject = (EnumObject) value;
        return (Integer)enumObject.ordinal;
      }
      return null;
    }

    public int asInt()        {
      if (id == INT_LITERAL) {
        return ((java.lang.Integer) value).intValue();
      } else if (id == ENUM_LITERAL) {
        return ((EnumObject)value).ordinal;
      } else {
        throw new IllegalStateException("Cannot map literal "  + this + " to int");
      }
    }
    public long asLong()      { return ((java.lang.Long)value).longValue(); }
    public float asFloat()    { return ((java.lang.Float)value).floatValue(); }
    public double asDouble()  { return ((java.lang.Double)value).doubleValue(); }
    public String asString()  {
      if (id == ENUM_LITERAL) {
        int ordinal = ((EnumObject)value).ordinal;
        return ((EnumObject)value).tags[ordinal];
      }
      return (java.lang.String)value;
    }
    public Buf asBuf()        { return (Buf)value; }
    public Type asType()      { return (Type)value; }
    public Slot asSlot()      { return (Slot)value; }
    public Object[] asArray() { return (Object[])value; }

    protected void doWalk(AstVisitor visitor)
    {
    }

    public Value toValue()
    {
      switch (id)
      {
        case TRUE_LITERAL:       return Bool.TRUE;
        case FALSE_LITERAL:      return Bool.FALSE;
        case INT_LITERAL:        return Int.make(asInt());
        case LONG_LITERAL:       return Long.make(asLong());
        case FLOAT_LITERAL:      return Float.make(asFloat());
        case DOUBLE_LITERAL:     return Double.make(asDouble());
        case TIME_LITERAL:       return Long.make(asLong());
        case STR_LITERAL:        return Str.make(value.toString());
        case BUF_LITERAL:        return Buf.fromString(asBuf().toString());
        case NULL_LITERAL:  
          if (type.isBool())     return Bool.NULL;
          if (type.isFloat())    return Float.NULL;                                 
          if (type.isDouble())   return Double.NULL;                                 
          throw new IllegalStateException("Cannot map null literal: " + type);
        default: throw new IllegalStateException("Cannot map to value: " + this);
      }
    }

    public String toCodeString()
    {
      switch (id)
      {
        case NULL_LITERAL:    return "null";
        case TIME_LITERAL:    return value.toString() + "ns";
        case SIZE_OF:         return asType().qname();
        case SLOT_ID_LITERAL: return value.toString();
        default:              return TypeUtil.toCodeString(value);
      }
    }

    public Expr.Literal negate()
    {
      Object newVal;
      switch (id)
      {
        case INT_LITERAL:    newVal = new java.lang.Integer(-asInt());   break;
        case LONG_LITERAL:   newVal = new java.lang.Long(-asLong());     break;
        case FLOAT_LITERAL:  newVal = new java.lang.Float(-asFloat());   break;
        case DOUBLE_LITERAL: newVal = new java.lang.Double(-asDouble()); break;
        case TIME_LITERAL:   newVal = new java.lang.Long(-asLong());     break;
        default: throw new IllegalStateException();
      }
      return new Expr.Literal(loc, id, type, newVal);
    }

    public int maxStack()
    {
      return type.isWide() ? 2 : 1;
    }

    public String toString()
    {                        
      return toCodeString();
    }

    public Object value;
  }

//////////////////////////////////////////////////////////////////////////
// Unary
//////////////////////////////////////////////////////////////////////////

  @SuppressWarnings("WeakerAccess")
  public static class Unary extends Expr
  {
    public Unary(Location loc, Token op, Expr operand) { this(loc, op, operand, false); }
    public Unary(Location loc, Token op, Expr operand, boolean postfix)
    {
      super(loc, op.toUnaryExprId(postfix));
      this.op = op;
      this.operand = operand;
    }

    public boolean isIncrDecr() { return isIncr() || isDecr(); }
    public boolean isIncr()     { return id == PRE_INCR  || id == POST_INCR; }
    public boolean isDecr()     { return id == PRE_DECR  || id == POST_DECR; }
    public boolean isPrefix()   { return id == PRE_INCR  || id == PRE_DECR; }
    public boolean isPostfix()  { return id == POST_INCR || id == POST_DECR; }

    @Override
    public boolean isConst() {
      if (operand instanceof Expr.Field) {
        // not const if lhs is this and op is either ++ or --
        return ! (((Field)operand).target instanceof Expr.This && isIncrDecr());
      }
      return true; // ow, 26.09.19
    }

    protected void doWalk(AstVisitor visitor)
    {
      operand = operand.walk(visitor);
    }

    public int maxStack()
    {              
      int x = operand.maxStack();   
      if (id == COND_NOT) x += 1;
      if (isIncrDecr()) x += type.isWide() ? 4 : 2;
      return x;
    }

    public String toString()
    {
      if (isPostfix())
        return operand.toString() + op.toString();
      else
        return op.toString() + operand.toString();
    }

    public Token op;
    public Expr operand;
  }

//////////////////////////////////////////////////////////////////////////
// Binary
//////////////////////////////////////////////////////////////////////////

  public static class Binary extends Expr
  {
    public Binary(Location loc, Token op, Expr lhs, Expr rhs)
    {
      super(loc, op.toBinaryExprId());
      this.op   = op;
      this.lhs  = lhs;
      this.rhs  = rhs;       
      this.type = lhs.type;
    }

    public boolean isAssign()
    {
      return op.isAssign();
    }

    protected void doWalk(AstVisitor visitor)
    {
      lhs = lhs.walk(visitor);
      rhs = rhs.walk(visitor);
    }

    public int maxStack()
    {
      int s = lhs.maxStack() + rhs.maxStack();
      if (isAssign()) s += type.isWide() ? 2 : 1; // for x += y assignments
      return s;
    }

    public String toString()
    {
      // ow, 21.09.19: Removed parenthesis
      //return "(" + lhs + " " + op + " " + rhs + ")";
      return lhs + " " + op + " " + rhs;
    }

    @Override
    public boolean isConst() {
      if (lhs instanceof Expr.Field) {
        // Binary is not const if lhs target is this/local and op is 'assign'
        // Local is not necessary the case, but cannot be evaluated in this context
        // We are optimistic and let the compiler speak. A missing const is much harder to detect
        // than a misplaced const.
        boolean isAssignmentToThis = ((Field)lhs).target instanceof Expr.This && op.isAssign();

        // boolean isAssignmentToLocal = ((Field)lhs).target instanceof Expr.Local && op.isAssign();
        return !isAssignmentToThis;
      }
      return true; // ow, 26.09.19
    }

    public Token op;
    public Expr lhs;
    public Expr rhs;
  }

//////////////////////////////////////////////////////////////////////////
// Cond
//////////////////////////////////////////////////////////////////////////

  @SuppressWarnings({"unchecked", "ForLoopReplaceableByForEach"})
  public static class Cond extends Expr
  {
    public Cond(Location loc, int id, Token op, Expr first)
    {
      super(loc, id);
      this.op  = op;
      this.operands.add(first);
    }

    protected void doWalk(AstVisitor visitor)
    {
      ArrayList newOperands = new ArrayList();
      for (int i=0; i<operands.size(); ++i)
      {
        Expr expr = (Expr)operands.get(i);
        newOperands.add(expr.walk(visitor));
      }
      operands = newOperands;
    }

    public int maxStack()
    {        
      int stack = 0;
      for (int i=0; i<operands.size(); ++i) 
        stack += ((Expr)operands.get(i)).maxStack();
      return stack;
    }

    public String toString()
    {
      StringBuilder s = new StringBuilder();
      s.append("(");
      for (int i=0; i<operands.size(); ++i)
      {
        if (i > 0) s.append(" ").append(op).append(" ");
        s.append(operands.get(i));
      }
      s.append(")");
      return s.toString();
    }

    @Override
    public boolean isConst() {
      return true; // ow, 26.09.19
    }

    public Token op;
    public ArrayList operands = new ArrayList();
  }

//////////////////////////////////////////////////////////////////////////
// Ternary
//////////////////////////////////////////////////////////////////////////

  public static class Ternary extends Expr
  {
    public Ternary(Location loc) { super(loc, TERNARY); }

    protected void doWalk(AstVisitor visitor)
    {
      cond      = cond.walk(visitor);
      trueExpr  = trueExpr.walk(visitor);
      falseExpr = falseExpr.walk(visitor);
    }

    public String toString()
    {
      return "(" + cond + " ? " + trueExpr + " : " + falseExpr + ")";
    }

    public int maxStack()
    {
      return Math.max(cond.maxStack(), 
        Math.max(trueExpr.maxStack(), falseExpr.maxStack()));
    }

    @Override
    public boolean isConst() {
      return true; // ow, 26.10.19
    }

    public Expr cond;
    public Expr trueExpr;
    public Expr falseExpr;
  }

//////////////////////////////////////////////////////////////////////////
// Name
//////////////////////////////////////////////////////////////////////////

  public static class Name extends Expr
  {
    public Name(Location loc, Expr target, String name) { this(loc, NAME, target, name); }
    public Name(Location loc, int id, Expr target, String name)
    {
      super(loc, id);
      this.target = target;
      this.name = name;
    }

    protected void doWalk(AstVisitor visitor)
    {
      if (target != null)
        target = target.walk(visitor);
    }

    public String toString()
    {
      if (target == null) return name;
      if (safeNav)
        return target.toString() + "?." + name;
      else
        return target.toString() + "." + name;
    }

    public int maxStack()
    {
      int maxStack = target == null ? 0 : target.maxStack();
      if (safeNav) maxStack++;
      return maxStack;
    }

    public Expr target;
    public String name;
    public boolean safeNav;  // is the ?. safe navigation operator
  }

//////////////////////////////////////////////////////////////////////////
// ParamVar
//////////////////////////////////////////////////////////////////////////

  @SuppressWarnings("UnnecessaryInterfaceModifier")
  public static interface NameDef
  {
    VarDef def();
  }
  
  public static class Param extends Name implements NameDef
  {
    public Param(Location loc, String name, ParamDef def)
    {
      super(loc, PARAM, null, name);
      this.type  = def.type;
      this.def   = def;
    }

    public boolean isAssignable() { return true; }

    public int maxStack() { return type.isWide() ? 2 : 1; }
    
    public VarDef def() { return def; }

    public ParamDef def;

    @Override
    public boolean isConst() {
      return true; // ow, 26..09.19
    }
  }

//////////////////////////////////////////////////////////////////////////
// Local
//////////////////////////////////////////////////////////////////////////

  public static class Local extends Name implements NameDef
  {
    public Local(Location loc, Stmt.LocalDef def)
    {
      super(loc, LOCAL, null, def.name);
      this.type  = def.type;
      this.def   = def;
    }

    public boolean isAssignable() { return true; }

    public int maxStack() { return type.isWide() ? 2 : 1; }
    
    public VarDef def() { return def; }

    public Stmt.LocalDef def;

    @Override
    public boolean isConst() {
      return true;
    }
  }

//////////////////////////////////////////////////////////////////////////
// This
//////////////////////////////////////////////////////////////////////////

  public static class This extends Expr
  {
    public This(Location loc)
    {
      super(loc, THIS);
    }

    public void doWalk(AstVisitor visitor) {}
    public int maxStack() { return 1; }
    public String toString() { return "this"; }

    @Override
    public boolean isConst() {
      return true;
    }
  }

//////////////////////////////////////////////////////////////////////////
// Super
//////////////////////////////////////////////////////////////////////////

  public static class Super extends Expr
  {
    public Super(Location loc)
    {
      super(loc, SUPER);
    }

    public void doWalk(AstVisitor visitor) {}
    public int maxStack() { return 1; }
    public String toString() { return "super"; }

    @Override
    public boolean isConst() {
      return true;
    }
  }

//////////////////////////////////////////////////////////////////////////
// Field
//////////////////////////////////////////////////////////////////////////

  public static class Field extends Name
  {
    public Field(Location loc, Expr target, sedonac.namespace.Field field, boolean safeNav)
    {
      super(loc, FIELD, target, field.name());
      this.field = field;
      this.type  = field.type();
      this.safeNav = safeNav;
    }

    public Field(Location loc, Expr target, sedonac.namespace.Field field)
    {
      this(loc, target, field, false);
    }

    public boolean isAssignable()
    {
      return !field.isInline() && !field.isConst();
    }

    public Expr.Literal toLiteral() 
    { 
      if (isDefine()) return field.define();
      return null;
    }

    public boolean isDefine()
    {
      return field.isDefine();
    }

    public Integer toIntLiteral() 
    { 
      if (isDefine() && type.isInt())  
        return field.define().toIntLiteral();
      return null;
    }

    @Override
    public boolean isConst() {
      return true;
    }

    public int maxStack()
    {                            
      int base = super.maxStack();                
      int x = type.isWide() ? 2 : 1;
      if (type.isByte() || type.isShort()) x++; // and mask in Java           
      return Math.max(base, x);
    }

    public String toString()
    {
      return super.toString();
    }

    public sedonac.namespace.Field field;      
  }

//////////////////////////////////////////////////////////////////////////
// Index
//////////////////////////////////////////////////////////////////////////

  @SuppressWarnings("RedundantIfStatement")
  public static class Index extends Expr
  {
    public Index(Location loc, Expr target, Expr index)
    {
      super(loc, INDEX);
      this.target = target;
      this.index  = index;
    }

    public boolean isAssignable() 
    { 
      if (target.id == FIELD && ((Field)target).field.isConst())
        return false;
      return true; 
    }

    @Override
    public boolean isConst() {
      return true; // ow, 26.09.19
    }

    protected void doWalk(AstVisitor visitor)
    {
      target = target.walk(visitor);
      index = index.walk(visitor);
    }

    public int maxStack() 
    { 
      return target.maxStack() + index.maxStack(); 
    }

    public String toString()
    {
      return target.toString() + "[" + index + "]";
    }

    public Expr target;
    public Expr index;
  }

//////////////////////////////////////////////////////////////////////////
// Call
//////////////////////////////////////////////////////////////////////////

  @SuppressWarnings("ConstantConditions")
  public static class Call extends Name
  {
    public Call(Location loc, Expr target, String name, Expr[] args)
    {
      super(loc, CALL, target, name);
      this.args = args;
    }

    public Call(Location loc, Expr target, Method method, Expr[] args)
    {
      this(loc, CALL, target, method, args);
    }

    public Call(Location loc, int id, Expr target, Method method, Expr[] args)
    {
      super(loc, id, target, method.name());
      this.method = method;
      this.args = args;
      if (method != null)
        this.type = method.returnType();
    }

    protected void doWalk(AstVisitor visitor)
    {
      super.doWalk(visitor);
      for (int i=0; i<args.length; ++i)
        args[i] = args[i].walk(visitor);
    }

    public int maxStack() 
    {                 
      int stack = super.maxStack();
      for (Expr arg : args) stack += arg.maxStack();

      if (id == Expr.PROP_SET) // if leave (which gets cleared in CodeAsm)
        stack += type.isWide() ? 2 : 1;
        
      Type ret = method.returnType();
      if (!ret.isVoid())
        stack = Math.max(stack, ret.isWide() ? 2 : 1);
      
      return stack; 
    }

    public String toString()
    {
      StringBuilder s = new StringBuilder(super.toString());
      s.append("(");
      for (int i=0; i<args.length; ++i)
      {
        if (i > 0) s.append(", ");
        s.append(args[i]);
      }
      return s.append(")").toString();
    }

    public Expr[] args;
    public Method method;    // resolved method
  }

//////////////////////////////////////////////////////////////////////////
// Cast
//////////////////////////////////////////////////////////////////////////

  public static class Cast extends Expr
  {
    public Cast(Location loc, Type type, Expr target)
    {
      super(loc, CAST);
      this.type   = type;
      this.target = target;
    }

    @Override
    public boolean isConst() {
      return true; // ow, 26.10.19
    }

    protected void doWalk(AstVisitor visitor)
    {
      type = visitor.type(type);
      target = target.walk(visitor);
    }

    public int maxStack() 
    {        
      int s = target.maxStack();
      if (type.isWide()) s = Math.max(s, 2);
      return s;
    }

    public String toString()
    {
      return "(" + type + ")" + target;
    }

    public Expr target;
  }

//////////////////////////////////////////////////////////////////////////
// InitArray
//////////////////////////////////////////////////////////////////////////

  public static class InitArray extends Expr
  {
    public InitArray(Location loc)
    {
      super(loc, INIT_ARRAY);
    }

    protected void doWalk(AstVisitor visitor) { }
    public String toString() { return "{...}"; }

    public int maxStack() { return 4; }

    public Expr field;   // push field address onto stack
    public Expr length;  // push length onto stack
  }

//////////////////////////////////////////////////////////////////////////
// InitVirt
//////////////////////////////////////////////////////////////////////////

  public static class InitVirt extends Expr
  {
    public InitVirt(Location loc, Type type)
    {
      super(loc, INIT_VIRT);
      this.type = type;
    }

    protected void doWalk(AstVisitor visitor) {}
    
    public int maxStack() { return 2; }
    
    public String toString() { return "InitVirt " + type; }
  }

//////////////////////////////////////////////////////////////////////////
// InitComp
//////////////////////////////////////////////////////////////////////////

  public static class InitComp extends Expr
  {
    public InitComp(Location loc, Type type)
    {
      super(loc, INIT_COMP);
      this.type = type;
    }

    protected void doWalk(AstVisitor visitor) {}
    public int maxStack() { return 2; }
    public String toString() { return "InitComp " + type; }
  }

//////////////////////////////////////////////////////////////////////////
// StaticType
//////////////////////////////////////////////////////////////////////////

  public static class StaticType extends Expr
  {
    public StaticType(Location loc, Type type)
    {
      super(loc, STATIC_TYPE);
      this.type = type;
    }

    @Override
    public boolean isConst() {
      return true; // ow, 26.09.19
    }

    protected void doWalk(AstVisitor visitor)
    {
      type = visitor.type(type);
    }

    public int maxStack() { return 0; }

    public String toString()
    {
      return type.toString();
    }
  }

//////////////////////////////////////////////////////////////////////////
// Interpolation
//////////////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public static class Interpolation extends Expr
  {
    public Interpolation(Location loc)
    {
      super(loc, INTERPOLATION);
      parts = new ArrayList();
    }

    @Override
    public boolean isConst() {
      return true; // ow, 26.10.19
    }

    public Expr.Literal first()
    {                                                 
      Expr first = (Expr)parts.get(0);
      if (first.id != STR_LITERAL) 
        throw new IllegalStateException("first not string literal: " + first.id + " " + first);
      return (Expr.Literal)first;
    }
    
    protected void doWalk(AstVisitor visitor)
    {                   
      ArrayList newParts = new ArrayList();
      for (Object part : parts) {
        Expr expr = (Expr) part;
        newParts.add(expr.walk(visitor));
      }
      parts = newParts;
    }

    public int maxStack() 
    {
      int stack = 0;
      for (Object part : parts) stack += ((Expr) part).maxStack();
      return stack;
    }

    public String toString()
    {             
      StringBuilder s = new StringBuilder();
      s.append('"');
      for (Object o : parts) {
        Expr part = (Expr) o;
        if (part.id == STR_LITERAL)
          s.append(part.toString());
        else
          s.append("${").append(part).append('}');
      }
      s.append('"');
      return s.toString();
    }                        
    
    public ArrayList parts;  // arbitrary expressions or string literals
    public Method[] printMethods;  // methods  for printXXX for each part
    public boolean callOk;   // true if we've checked this is arg to OutStream.print
  }

//////////////////////////////////////////////////////////////////////////
// New
//////////////////////////////////////////////////////////////////////////

  public static class New extends Expr
  {
    public New(Location loc)
    {
      super(loc, NEW);                  
    }                
        
    protected void doWalk(AstVisitor visitor)
    {
      of = visitor.type(of);    
      if (arrayLength != null)
        arrayLength = arrayLength.walk(visitor);
    }

    public String toString()
    {      
      String s = "new " + of;
      if (arrayLength != null) s += "[" + arrayLength + "]";
      else s += "()";
      return s;
    }

    public int maxStack() 
    {
      return 2;
    }

    public Type of;
    public Expr arrayLength;
  }

//////////////////////////////////////////////////////////////////////////
// Delete
//////////////////////////////////////////////////////////////////////////

  public static class Delete extends Expr
  {
    public Delete(Location loc)
    {
      super(loc, DELETE);                  
    }                
        
    protected void doWalk(AstVisitor visitor)
    {       
      target = target.walk(visitor);
    }

    public int maxStack() 
    {
      return target.maxStack();
    }

    public String toString()
    {      
      return "delete " + target;
    }

    public Expr target;
  }

//////////////////////////////////////////////////////////////////////////
// Enum
//////////////////////////////////////////////////////////////////////////
  public static class EnumObject
  {
    public EnumObject(String name, int ordinal, String[] tags) {
      this.name = name;
      this.ordinal = ordinal;
      this.tags = tags;
    }

    /**
     * Name of the enum class/type
     */
    public String name;
    /**
     * The ordinal value
     */
    public int ordinal;
    public String[] tags;
  }

//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

  public int id;
  public Type type;
  public boolean leave = true;

}
