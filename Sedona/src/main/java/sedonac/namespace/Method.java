//
// Copyright (c) 2007 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   5 Mar 07  Brian Frank  Creation
//

package sedonac.namespace;

/**
 * Method is the interace for classes which represent a method
 * such as IrMethod or MethodDef.
 */
public interface Method
  extends Slot
{

  /**
   * Get the return type of the method.
   */
  public Type returnType();

  /**
   * Get the parameter types of the method.
   */
  public Type[] paramTypes();
  
  /**
   * Get the number of parameters which takes into 
   * account the implicit this and wide parameters.
   */
  public int numParams();

  /**
   * Is the name INSTANCE_INIT
   */
  public boolean isInstanceInit();

  /**
   * Is the name STATIC_INIT
   */
  public boolean isStaticInit();

  /**
   * Is the name INSTANCE_DESTROY (ow, 27.09.19)
   */
  public boolean isInstanceDestroy();

  public static final String INSTANCE_INIT = "_iInit";
  public static final String STATIC_INIT = "_sInit";
  public static final String INSTANCE_DESTROY = "_iDestroy";

  /**
   * Method is init or destroy.
   * @return true, if this method is either a (static) constructor or a destructor
   */
  public boolean isInitOrDestroy();
}
