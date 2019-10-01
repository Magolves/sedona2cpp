/*********************************************************
 * Implementation for class 'Component'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Component
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "Component.h"
#include "Link.h"
#include "Slot.h"
#include "Component.h"
#include "Sys.h"
#include "InStream.h"
#include "OutStream.h"
#include "Buf.h"
#include "App.h"
#include "Err.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'Component 
 * _iInit
 * '
 */
Component::Component() {
    ;
    /* Init virt InitVirt sys::Component */;
    ;
    (this->children) = static_cast<uint16_t>((Component::nullId));
    (this->meta) = 1;
    ;
    (this->nextSibling) = static_cast<uint16_t>((Component::nullId));
    (this->parent) = static_cast<uint16_t>((Component::nullId));
}


/**
 * Get the path of this component from the root App.
 * The end of the path is denoted by null.  The list
 * is a shared static buffer. 
 * Returns empty vector on error or
 * if not mounted under an Sys.app.
 */
vector<Component>& Component::path() {
    int32_t depth;
    Component* p;

    depth = 0;
    p = this;
	pathBuf.clear();

	for (p = this; p != nullptr; p = Sys::app.lookup(p->parent)) {
		pathBuf.insert(pathBuf.begin(), *p);
	}
    
    return (Component::pathBuf);
}


/**
 * Lookup a child by name or return null.
 */
Component* Component::lookupByName(const string name) {
    Component* c;

    App& app = Sys::app;
    for (c = /* local */app.lookup((this->children)); c != nullptr; c = /* local */app.lookup((c->nextSibling)))  {
        if (this->name == name) {
            return c;
        }
    }
    return nullptr;
}


/**
 * Invoke an action which takes no arguments.
 */
void Component::invokeVoid(Slot& slot)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Invoke an action which takes a boolean argument.
 */
void Component::invokeBool(Slot& slot, bool arg)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Invoke an action which takes an int argument.
 */
void Component::invokeInt(Slot& slot, int32_t arg)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Invoke an action which takes a long argument.
 */
void Component::invokeLong(Slot& slot, int64_t arg)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Invoke an action which takes a float argument.
 */
void Component::invokeFloat(Slot& slot, float arg)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Invoke an action which takes a double argument.
 */
void Component::invokeDouble(Slot& slot, double arg)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Invoke an action which takes a Buf argument.
 */
void Component::invokeBuf(Slot& slot, const Buf* arg)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Get a bool property using reflection.
 */
bool Component::getBool(Slot& slot)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Get an integer 
 * byte, short, or int
 *  property using reflection.
 */
int32_t Component::getInt(Slot& slot)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Get a long property using reflection.
 */
int64_t Component::getLong(Slot& slot)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Get a float property using reflection.
 */
float Component::getFloat(Slot& slot)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Get a double property using reflection.
 */
double Component::getDouble(Slot& slot)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Get a Buf property using reflection.
 */
Buf* Component::getBuf(Slot& slot)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Set a bool property using reflection.
 */
void Component::setBool(Slot& slot, bool val) {
    if (doSetBool(slot, val)) {
        slotChanged(slot);
    }
}


/**
 * Set an integer 
 * byte, short, or int
 *  property using reflection.
 */
void Component::setInt(Slot& slot, int32_t val) {
    if (doSetInt(slot, val)) {
        slotChanged(slot);
    }
}


/**
 * Set a long property using reflection.
 */
void Component::setLong(Slot& slot, int64_t val) {
    if (doSetLong(slot, val)) {
        slotChanged(slot);
    }
}


/**
 * Set a float property using reflection.
 */
void Component::setFloat(Slot& slot, float val) {
    if (doSetFloat(slot, val)) {
        slotChanged(slot);
    }
}


/**
 * Set a double property using reflection.
 */
void Component::setDouble(Slot& slot, double val) {
    if (doSetDouble(slot, val)) {
        slotChanged(slot);
    }
}


/**
 * Implementation of method 'doSetBool'
 */
bool Component::doSetBool(Slot& slot, bool val)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Implementation of method 'doSetInt'
 */
bool Component::doSetInt(Slot& slot, int32_t val)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Implementation of method 'doSetLong'
 */
bool Component::doSetLong(Slot& slot, int64_t val)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Implementation of method 'doSetFloat'
 */
bool Component::doSetFloat(Slot& slot, float val)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * Implementation of method 'doSetDouble'
 */
bool Component::doSetDouble(Slot& slot, double val)/* / Native or external function call */
{ throw "Not implemented yet"; } 


/**
 * The changed method must be called when a slot is modified.
 * It is automatically called whenever setting a property
 * field with a primitive type 
 * bool, byte, short, int, and
 * float
 * .  However it must be manually called after updating
 * the contents of a Buf property.  If you override this
 * method, you MUST call super!
 */
void Component::changed(Slot& slot) {
    Sys::orBytes(slot.watchEvent(), (this->watchFlags), 0, (Watch::max));
}


/**
 * Implementation of method 'slotChanged'
 */
void Component::slotChanged(Slot& slot) {
    if (Sys::app.isRunning()) {
        changed(slot);
    }
}


/**
 * Set property to a default value.  
 * Called when a link is removed from an input slot.
 */
void Component::setToDefault(Slot& slot) {
}


/**
 * Callback when component is loaded into an app.  This occurs
 * before the start phase.  Only called if app is running.
 */
void Component::loaded() {
}


/**
 * Callback when component is first started in an app.  This occurs
 * only after all components have had their loaded callback invoked. 
 * Only called if app is running
 */
void Component::start() {
}


/**
 * Callback when component is first stopped in an app.  Only called
 * if app is running.
 */
void Component::stop() {
}


/**
 * Execute is called once every scan using the
 * simple round-robin scan engine.
 */
void Component::execute() {
}


/**
 * Propagate data across links to myself.
 */
void Component::propagateLinksTo() {
    Link* link;

    for (link = (this->linksTo); link != nullptr; link = (link->nextTo))  {
        /* local */link->propagate();
    }
}


/**
 * allowChildExecute returns false if child components of this
 * should not have execute
 * 
 *  called this app cycle.
 */
bool Component::allowChildExecute() {
    return true;
}


/**
 * Called on parent when a child event occurs.  Only called if app is
 * running.
 * 
 * Defined event types are:
 * 
 * REMOVED - notification that child has been removed from component.  
 *   Should always return 0.  Called after stop
 * 
 *  on child.
 * ADDED   - notification that child has been added to component.  
 *   Should always return 0.   Called prior to loaded
 * 
 *  / start
 * 
 *  on child.
 * REORDERED - notification that component's children have been reordered.  
 *   Should always return 0.   Called after the reordering is complete.
 *   Only called once per reorder event; child arg is always null.
 * 
 * Future event types may make use of return code.
 */
int32_t Component::childEvent(int32_t eType, const Component& child) {
    return 0;
}


/**
 * Called on a child when a parent event occurs.  Only called if app is
 * running.
 * 
 * Defined event types are:
 * 
 * REMOVED - notification that child has been removed/unparented.  
 *   Should always return 0.  Called after stop
 * 
 *  on child.
 * ADDED   - notification this child has been parented.  
 *   Should always return 0.  Called prior to loaded
 * 
 *  / start
 * 
 *  on child.
 * 
 * Future event types may make use of return code.
 */
int32_t Component::parentEvent(int32_t eType, const Component& parent) {
    return 0;
}


/**
 * Called on a component when a link event occurs.  Only called
 * if app is running.
 * 
 * Defined event types are:
 * 
 * REMOVED - link has been removed from the component.  
 *   Should always return 0
 * ADDED   - link has been added to the component.  
 *   Should always return 0. 
 * 
 * Future event types may make use of return code.
 */
int32_t Component::linkEvent(int32_t eType, const Link* link) {
    return 0;
}


/**
 * Fire a tree changed event on this component by marking
 * the tree event bit for each watch's bitmask.
 */
void Component::fireTreeChanged() {
    Sys::orBytes((Watch::eventTree), (this->watchFlags), 0, (Watch::max));
}


/**
 * Fire the links changed event on this component by marking
 * the link event bit for each watch's bitmask.
 */
void Component::fireLinksChanged() {
    Sys::orBytes((Watch::eventLinks), (this->watchFlags), 0, (Watch::max));
}


/**
 * Save the component's application information in
 * binary format to the output stream:
 * 
 *   appComp
 *   {
 *     u2      id
 *     u1      kitId
 *     u1      typeId
 *     str     name
 *     u2      parent
 *     u2      children
 *     u2      nextSibling
 *     val[]   configProps
 *     u1      ';' end marker
 *   }
 */
void Component::saveAppComp(OutStream* out) {
    out->writeI2((this->id));
	// TODO
    //out->write((((this->type)->kit)->id));
    //out->write(((this->type)->id));
    out->writeStr((this->name));
    out->writeI2((this->parent));
    out->writeI2((this->children));
    out->writeI2((this->nextSibling));
    saveProps(out, 99);
    out->write(59);
}


/**
 * Save the property values to the output stream.
 * Filter:
 *   0   = all
 *   'c' = config only
 *   'r' = runtime only
 */
void Component::saveProps(OutStream* out, int32_t filter) {
}


/**
 * Save a property value to the output stream.
 */
void Component::saveProp(OutStream* out, Slot& prop) {
}


/**
 * Load the component's configuration from a binary
 * format input stream - see saveAppComp
 * 
 *  for format.
 * We assume component id and type id have already been
 * read.  Return 0 on success or non-zero on error.
 */
int32_t Component::loadAppComp(InStream* in) {
    if (!in->readStr((this->name), 8)) {
        return (Err::nameTooLong);
    }
    (this->parent) = static_cast<uint16_t>(in->readU2());
    (this->children) = static_cast<uint16_t>(in->readU2());
    (this->nextSibling) = static_cast<uint16_t>(in->readU2());
    loadProps(in, 99);
    return (in->read() == 59) ? 0 : (Err::invalidCompEndMarker);
}


/**
 * Load the property values to the output stream.
 * Filter:
 *   0   = all
 *   'c' = config only
 *   'r' = runtime only
 */
void Component::loadProps(InStream* in, int32_t filter) {
}


/**
 * Decode a value from the input stream
 * and set for the specified property.
 */
void Component::loadProp(InStream* in, Slot& slot) {
}


/**
 * Decode a value from the input stream
 * and invoke for the specified action.
 */
void Component::invokeAction(InStream* in, Slot& slot) {
}


/**
 * Copy constructor for 'Component'
 */
/**
 * Move constructor for 'Component'
 */
/**
 * Destructor
 */
Component::~Component() {}

const int32_t Component::ADDED = 1;
const int32_t Component::REMOVED = 0;
const int32_t Component::REORDERED = 2;
const int32_t Component::nameLen = 8;
const int32_t Component::nullId = 65535;
vector<Component> Component::pathBuf = {} /* DEF */;
const int32_t Component::pathBufLen = 16;
const string Component::TYPE_NAME = "sys::Component";
const string Component::BASE_TYPE_NAME = "sys::Virtual";

} // namespace sys
