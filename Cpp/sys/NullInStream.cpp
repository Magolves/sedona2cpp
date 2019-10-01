/*********************************************************
 * Implementation for class 'NullInStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : NullInStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "NullInStream.h"
#include "InStream.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'NullInStream 
 * _iInit
 * '
 */
NullInStream::NullInStream() {
    ;
    /* Init virt InitVirt sys::NullInStream */;
}


/**
 * Implementation of method 'read'
 */
int32_t NullInStream::read() {
    return -1;
}


/**
 * Implementation of method 'readBytes'
 */
int32_t NullInStream::readBytes(vector<uint8_t>& b, int32_t off, int32_t len) {
    return -1;
}


/**
 * Copy constructor for 'NullInStream'
 */
/**
 * Move constructor for 'NullInStream'
 */
/**
 * Destructor
 */
NullInStream::~NullInStream() {}

} // namespace sys
