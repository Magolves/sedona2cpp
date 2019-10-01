/*********************************************************
 * Implementation for class 'Link'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Link
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "Link.h"
#include "Slot.h"

#include "Component.h"
#include "InStream.h"
#include "OutStream.h"
#include "App.h"
#include "Sys.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'Link 
 * _iInit
 * '
 */
Link::Link() {
}


/**
 * Propage the link
 */
void Link::propagate() {
    Component* from;
    Component* to;
    Slot* fromSlot;
    Slot* toSlot;

    from = Sys::app.lookup((this->fromComp));
    to = Sys::app.lookup((this->toComp));
    if ((from == nullptr)||(to == nullptr)) {
    }
	/* TODO
    fromSlot = type->slot((this->fromSlot));
    toSlot = type->slot((this->toSlot));
    if ((fromSlot == nullptr)||(toSlot == nullptr)) {
    }
	if (fromSlot->isProp()) {
		if (toSlot->isProp()) {
			switch (((toSlot->type)->id)) {
			case sys::Str.boolId:
				to.setBool(toSlot, from.getBool(fromSlot));
				break;
			case sys::Str.byteId:
				to.setInt(toSlot, from.getInt(fromSlot));
				break;
			case sys::Str.shortId:
				to.setInt(toSlot, from.getInt(fromSlot));
				break;
			case sys::Str.intId:
				to.setInt(toSlot, from.getInt(fromSlot));
				break;
			case sys::Str.longId:
				to.setLong(toSlot, from.getLong(fromSlot));
				break;
			case sys::Str.floatId:
				to.setFloat(toSlot, from.getFloat(fromSlot));
				break;
			case sys::Str.doubleId:
				to.setDouble(toSlot, from.getDouble(fromSlot));
				break;
			} // switch
		}
		else {
		}
	}*/
}


/**
 * Save the link in binary format to the output stream.
 */
void Link::save(OutStream* out) {
    out->writeI2((this->fromComp));
    out->write((this->fromSlot));
    out->writeI2((this->toComp));
    out->write((this->toSlot));
}


/**
 * Load the link from a binary format input stream.
 * Return true on success, false on error.
 */
bool Link::load(InStream* in, int32_t fromComp) {
    int32_t x;

    (this->fromComp) = static_cast<uint16_t>(fromComp);
    (this->fromSlot) = static_cast<uint8_t>(in->read());
    (this->toComp) = static_cast<uint16_t>(in->readU2());
    x = in->read();
    (this->toSlot) = static_cast<uint8_t>(x);
    return x >= 0;
}


/**
 * Copy constructor for 'Link'
 */
/**
 * Move constructor for 'Link'
 */
} // namespace sys
