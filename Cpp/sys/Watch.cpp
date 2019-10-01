/*********************************************************
 * Implementation for class 'Watch'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Watch
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "Watch.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'Watch 
 * _iInit
 * '
 */
Watch::Watch() {
    ;
    /* Init virt InitVirt sys::Watch */;
}


/**
 * The watch id is a combination of the count in the
 * high byte and the index in the low byte.
 */
int32_t Watch::id() {
    return ((this->rand) << 8) | (this->index);
}


/**
 * Map a subscription bit to its ASCII code.
 */
int32_t Watch::fromSubBit(int32_t subBit) {
    if (subBit == (Watch::subTree)) {
        return 116;
    }
    if (subBit == (Watch::subConfig)) {
        return 99;
    }
    if (subBit == (Watch::subRuntime)) {
        return 114;
    }
    if (subBit == (Watch::subLinks)) {
        return 108;
    }
    return 33;
}


/**
 * Map ASCII codes to subscription bit:
 *  't' = subTree
 *  'c' = subConfig
 *  'r' = subRuntime
 *  'l' = subLink
 *  '*' = subAll
 */
int32_t Watch::toSubBit(int32_t what) {
    return Watch::toEventBit(what) << 4;
}


/**
 * Map ASCII codes to event bit:
 *  't' = eventTree
 *  'c' = eventConfig
 *  'r' = eventRuntime
 *  'l' = eventLink
 *  '*' = eventAll
 */
int32_t Watch::toEventBit(int32_t what) {
    if (what == 116) {
        return (Watch::eventTree);
    }
    if (what == 99) {
        return (Watch::eventConfig);
    }
    if (what == 114) {
        return (Watch::eventRuntime);
    }
    if (what == 108) {
        return (Watch::eventLinks);
    }
    if (what == 42) {
        return (Watch::eventAll);
    }
    return 0;
}


/**
 * Copy constructor for 'Watch'
 */
/**
 * Move constructor for 'Watch'
 */
/**
 * Destructor
 */
Watch::~Watch() {}

const int32_t Watch::eventAll = 15;
const int32_t Watch::eventConfig = 2;
const int32_t Watch::eventLinks = 8;
const int32_t Watch::eventRuntime = 4;
const int32_t Watch::eventTree = 1;
const int32_t Watch::max = 4;
const int32_t Watch::subAll = 240;
const int32_t Watch::subConfig = 32;
const int32_t Watch::subLinks = 128;
const int32_t Watch::subRuntime = 64;
const int32_t Watch::subTree = 16;
} // namespace sys
