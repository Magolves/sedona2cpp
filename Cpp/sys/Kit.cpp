/*********************************************************
 * Implementation for class 'Kit'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Kit
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "Kit.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'Kit 
 * _iInit
 * '
 */
Kit::Kit() {
}


/**
 * Get the type for the specified id or return null if out of range.
 */
string Kit::type(int32_t id) {
    if ((0 <= id)&&(id < (this->typesLen))) {
        return (this->types)[id];
    }
    return nullptr;
}


/**
 * Find a type by its simple name unique to this
 * kit or return null if not found.
 */
string Kit::findType(const string name) {
    return nullptr;
}


/**
 * Copy constructor for 'Kit'
 */
/**
 * Move constructor for 'Kit'
 */
} // namespace sys
