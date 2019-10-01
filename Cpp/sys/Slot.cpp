/*********************************************************
 * Implementation for class 'Slot'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Slot
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "Slot.h"
#include "Watch.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'Slot 
 * _iInit
 * '
 *
Slot::Slot() {
}*/


/**
 * Is this a property slot.
 */
bool Slot::isProp() {
    return ((this->flags) & (Slot::ACTION)) == 0;
}


/**
 * Is this an action slot.
 */
bool Slot::isAction() {
    return ((this->flags) & (Slot::ACTION)) != 0;
}


/**
 * Is this a config property.
 */
bool Slot::isConfig() {
    return ((this->flags) & (Slot::CONFIG)) != 0;
}


/**
 * Is this a Buf property which should be treated as a Str.
 */
bool Slot::isAsStr() {
    return ((this->flags) & (Slot::AS_STR)) != 0;
}


/**
 * Is true if an operator level slot, or false if admin level.
 */
bool Slot::isOperator() {
    return ((this->flags) & (Slot::OPERATOR)) != 0;
}


/**
 * Return if this slot is a property against the specified
 * filter:
 *    '*' = any property
 *    'c' = only config properties
 *    'r' = only runtime properties
 *    'C' = only operator level config properties
 *    'R' = only operator level runtime properties
 */
bool Slot::matchProp(int32_t filter) {
    if (isProp()) {
        if (filter == 42) {
            return true;
        }
        if (filter == 99) {
            return ((this->flags) & (Slot::CONFIG)) != 0;
        }
        if (filter == 114) {
            return ((this->flags) & (Slot::CONFIG)) == 0;
        }
        if (filter == 67) {
            return ((this->flags) & ((Slot::CONFIG) | (Slot::OPERATOR))) == ((Slot::CONFIG) | (Slot::OPERATOR));
        }
        if (filter == 82) {
            return ((this->flags) & ((Slot::CONFIG) | (Slot::OPERATOR))) == (Slot::OPERATOR);
        }
    }
    return false;
}

/**
 * Get the event mask for the property type.
 */
int Slot::watchEvent()
{
	return ((flags & CONFIG) != 0) ? Watch::eventConfig : Watch::eventRuntime;
}


/**
 * Copy constructor for 'Slot'
 */
/**
 * Move constructor for 'Slot'
 */
const int32_t Slot::ACTION = 1;
const int32_t Slot::AS_STR = 4;
const int32_t Slot::CONFIG = 2;
const int32_t Slot::OPERATOR = 8;
} // namespace sys
