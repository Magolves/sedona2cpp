/*********************************************************
 * Implementation for class 'NullOutStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : NullOutStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "NullOutStream.h"
#include "OutStream.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'NullOutStream 
 * _iInit
 * '
 */
NullOutStream::NullOutStream() {
    ;
    /* Init virt InitVirt sys::NullOutStream */;
}


/**
 * Implementation of method 'write'
 */
bool NullOutStream::write(int32_t b) {
    return false;
}


/**
 * Implementation of method 'writeBytes'
 */
bool NullOutStream::writeBytes(vector<uint8_t>& b, int32_t off, int32_t len) {
    return false;
}


/**
 * Copy constructor for 'NullOutStream'
 */
/**
 * Move constructor for 'NullOutStream'
 */
/**
 * Destructor
 */
NullOutStream::~NullOutStream() {}

} // namespace sys
