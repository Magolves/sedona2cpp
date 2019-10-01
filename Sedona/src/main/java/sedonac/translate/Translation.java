//
// Copyright (c) 2007 Tridium, Inc.
// Licensed under the Academic Free License version 3.0
//
// History:
//   15 Mar 07  Brian Frank  Creation
//

// this code is in a vegetative state

package sedonac.translate;

import java.io.*;
import java.util.HashMap;

import sedonac.ast.*;

/**
 * Translation stores information about a Sedona to C translation.
 */
public class Translation
{

  public String target;    // "c", maybe "cpp"?
  public String main;      // main method qualified name
  public File outDir;      // where to generate output
  public KitDef[] kits;    // list of kit AST trees
  public int activeKitIndex;    // kit which is currently translated

  public CppOptions cppOptions = new CppOptions();

  public boolean isCpp() {
    return "c++".equals(target) || "cpp".equals(target);
  }
}
