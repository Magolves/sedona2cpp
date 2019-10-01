/*********************************************************
 * Implementation for class 'StdOutStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : StdOutStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "StdOutStream.h"
#include "OutStream.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'StdOutStream 
 * _iInit
 * '
 */
StdOutStream::StdOutStream() {
    ;
    /* Init virt InitVirt sys::StdOutStream */;
}


/**
 * Implementation of method 'write'
 */
bool StdOutStream::write(int32_t b) {
    return StdOutStream::doWrite(b);
}


/**
 * Implementation of method 'writeBytes'
 */
bool StdOutStream::writeBytes(vector<uint8_t>& b, int32_t off, int32_t len) {
    return StdOutStream::doWriteBytes(b, off, len);
}


/**
 * Implementation of method 'flush'
 */
void StdOutStream::flush() {
    StdOutStream::doFlush();
}


/**
 * Implementation of method 'doWrite'
 */
bool StdOutStream::doWrite(int32_t b)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Implementation of method 'doWriteBytes'
 */
bool StdOutStream::doWriteBytes(vector<uint8_t>& b, int32_t off, int32_t len)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Implementation of method 'doFlush'
 */
void StdOutStream::doFlush()/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Copy constructor for 'StdOutStream'
 */
/**
 * Move constructor for 'StdOutStream'
 */
/**
 * Destructor
 */
StdOutStream::~StdOutStream() {}

} // namespace sys
