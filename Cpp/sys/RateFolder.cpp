/*********************************************************
 * Implementation for class 'RateFolder'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : RateFolder
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "RateFolder.h"
#include "Folder.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'RateFolder 
 * _iInit
 * '
 */
RateFolder::RateFolder() {
    (this->appCyclesToSkip) = 0;
    (this->execCount) = 0;
}


/**
 * Override loaded
 * 
 *  to init execCount.
 */
void RateFolder::loaded() {
    execCount = this->appCyclesToSkip.get();
}


/**
 * Calculate when to allow children to execute.
 */
bool RateFolder::allowChildExecute() {
    if (execCount <= 0) {
        execCount = this->appCyclesToSkip.get();
        return true;
    }
    execCount--;
    return false;
}


/**
 * Copy constructor for 'RateFolder'
 */
/**
 * Move constructor for 'RateFolder'
 */
/**
 * Destructor
 */
RateFolder::~RateFolder() {}

const string RateFolder::TYPE_NAME = "sys::RateFolder";
const string RateFolder::BASE_TYPE_NAME = "sys::Folder";

} // namespace sys
