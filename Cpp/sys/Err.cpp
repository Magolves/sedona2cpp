/*********************************************************
 * Implementation for class 'Err'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Err
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "Err.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'Err 
 * _iInit
 * '
 */
Err::Err() {
}


/**
 * Copy constructor for 'Err'
 */
/**
 * Move constructor for 'Err'
 */
const int32_t Err::badPlatformService = 54;
const int32_t Err::cannotInitApp = 41;
const int32_t Err::cannotInsert = 50;
const int32_t Err::cannotLoadLink = 51;
const int32_t Err::cannotMalloc = 49;
const int32_t Err::cannotOpenFile = 42;
const int32_t Err::hibernate = 255;
const int32_t Err::invalidAppEndMarker = 52;
const int32_t Err::invalidArgs = 40;
const int32_t Err::invalidCompEndMarker = 60;
const int32_t Err::invalidKitId = 47;
const int32_t Err::invalidMagic = 43;
const int32_t Err::invalidSchema = 45;
const int32_t Err::invalidTypeId = 48;
const int32_t Err::invalidVersion = 44;
const int32_t Err::nameTooLong = 61;
const int32_t Err::noPlatformService = 53;
const int32_t Err::restart = 254;
const int32_t Err::unexpectedEOF = 46;
const int32_t Err::yield = 253;
} // namespace sys
