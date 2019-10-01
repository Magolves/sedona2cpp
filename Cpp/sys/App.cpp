/*********************************************************
 * Implementation for class 'App'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : App
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "App.h"
#include "Link.h"
#include "Slot.h"
#include "Component.h"
#include "Sys.h"
#include "InStream.h"
#include "Log.h"
#include "OutStream.h"
#include "PlatformService.h"
#include "Watch.h"
#include "Buf.h"
#include "Kit.h"
#include "Service.h"
#include "Err.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'App 
 * _iInit
 * '
 */
App::App() {
    (this->atSteadyState) = false;
    (this->guardTime) = 5;
    (this->hibernationResetsSteadyState) = false;
	(this->kitIdMap) = {};
    (this->lastEndWork) = 0;
    (this->lastStartExec) = 0;
    (this->lastStartWork) = 0;
    (this->newStartExec) = 0;
    (this->runStatus) = static_cast<uint8_t>(0);
    (this->scanPeriod) = 50;
    (this->steadyAt) = 0;
    (this->timeToSteadyState) = 0;
}


/**
 * Prepare the applications data structures to begin
 * configuration.
 */
bool App::initApp(int32_t initCompsLen) {
    (this->id) = static_cast<uint16_t>(0);
    (this->parent) = static_cast<uint16_t>((Component::nullId));
    name = "App";
    if (!ensureCompsCapacity(initCompsLen)) {
        return false;
    }
    (this->comps)[0] = *this;
    return true;
}


/**
 * Free all the dynamic memory associated with
 * this application.
 */
void App::cleanupApp() {
    (this->services) = nullptr;
    (this->children) = static_cast<uint16_t>((Component::nullId));
    (this->compsLen) = 0;

	// TODO: Delete components?
	comps.clear();

}


/**
 * Is the application currently running
 */
bool App::isRunning() {
    return (this->running);
}


/**
 * Has the application reached steady state 
 * as defined by timeToSteadyState
 */
bool App::isSteadyState() {
    if (!(this->atSteadyState)) {
        (this->atSteadyState) = ((this->running)&&(Sys::ticks() > (this->steadyAt)));
    }
    return (this->atSteadyState);
}


/**
 * Start all the components.  Return 0 on success,
 * error code on failure.
 */
int32_t App::startApp(const vector<string>& args, int32_t argsLen) {
    std::string expected;
    int32_t r;

    (this->platform) = static_cast<PlatformService*>(lookupService("sys::PlatformService"));
    if ((this->platform) == nullptr) {
        return (Err::noPlatformService);
    }
	/*
    expected = Sys::findType(Sys::platformType());
    if (expected != Sys::platformType()) {
        log.error("missing platform type: " + Sys::platformType() + "\n");
    }
	
    if ((expected == nullptr)||!type->is(expected)) {
        return (Err::badPlatformService);
    }*/
    r = platform->init(args, argsLen);
    if (r != 0) {
        return r;
    }
    log.message("starting");
    platform->notify("app", "starting");
    return 0;
}


/**
 * Stop all the components.
 */
void App::stopApp() {
    log.message("stopping");
    platform->notify("app", "stopping");
}


/**
 * Run this application - right now this is a
 * simple round robin execution.
 */
int32_t App::runApp() {
    log.message("running");
    platform->notify("app", "running");
    (this->steadyAt) = (Sys::ticks() + (static_cast<int64_t>((this->timeToSteadyState) * 1000000) /* TODO: Check time unit (ns) */));
    return resumeApp();
}


/**
 * Run this application - right now this is a
 * simple round robin execution.
 */
int32_t App::resumeApp() {
    int64_t deadline;
    int64_t nsUntilDeadline;

    if ((this->runStatus) == (Err::hibernate)) {
        Service* s;

        if ((this->hibernationResetsSteadyState) == true) {
            (this->atSteadyState) = false;
            (this->steadyAt) = (Sys::ticks() + (static_cast<int64_t>((this->timeToSteadyState) * 1000000) /* TODO: Check time unit (ns) */));
        }
        for (s = (this->services); s != nullptr; s = (s->nextService))  {
            s->onUnhibernate();
        }
    }
    (this->running) = true;
    (this->runStatus) = static_cast<uint8_t>(0);
    deadline = Sys::ticks();
    nsUntilDeadline = 0;
    (this->lastEndWork) = deadline;
    while ((this->running))  {
        bool more;
        bool canHibernate;

        (this->cycleCount)++;
        (this->lastStartExec) = (this->newStartExec);
        (this->newStartExec) = Sys::ticks();
        deadline = (deadline + (static_cast<int64_t>((this->scanPeriod) * 1000000) /* TODO: Check time unit (ns) */));
        executeTree(*this);
        (this->lastStartWork) = Sys::ticks();
        more = true;
        canHibernate = true;
        while (more && (this->running))  {
            Service* s;

            more = false;
            for (s = (this->services); s != nullptr; s = (s->nextService))  {
                more |= s->work();
                canHibernate &= s->canHibernate();
            }
            if (canHibernate) {
                (this->runStatus) = static_cast<uint8_t>((Err::hibernate));
            }
            nsUntilDeadline = (deadline - Sys::ticks());
            if (nsUntilDeadline < (static_cast<int64_t>((this->guardTime) * 1000000) /* TODO: Check time unit (ns) */)) {
                break;
            }
            if (platform->workDuringFreeTime(nsUntilDeadline)) {
                more = true;
            }
        }
        (this->lastEndWork) = Sys::ticks();
        if ((this->runStatus) == (Err::hibernate)) {
            Service* s;

            for (s = (this->services); s != nullptr; s = (s->nextService))  {
                s->onHibernate();
            }
            return (this->runStatus);
        }
         else  {
            if (platform->yieldRequired()) {
                platform->yield(nsUntilDeadline);
                (this->runStatus) = static_cast<uint8_t>((Err::yield));
                return (this->runStatus);
            }
             else  {
                Sys::sleep(nsUntilDeadline);
            }
        }
    }
    return (this->runStatus);
}


/**
 * Recursively execute the component and its children.
 */
void App::executeTree(Component& c) {

    if (((c.children) != (Component::nullId)) && c.allowChildExecute()) {
        Component* kid;

        kid = lookup((c.children));
        while (kid != nullptr)  {
            executeTree(*kid);
            kid = lookup((kid->nextSibling));
        }
    }
    c.propagateLinksTo();
    c.execute();
}


/**
 * Lookup a component by id or null.
 */
Component* App::lookup(int32_t cid) {
    if ((cid < 0) || (cid >= (this->compsLen))) {
        return nullptr;
    }
    return &(this->comps)[cid];
}


/**
 * Get the maximum component id used by the application.
 */
int32_t App::maxId() {
    return comps.size() - 1;
}


/**
 * Lookup a service by type 
 * or base type
 * .  Return
 * null if there are no registered services which
 * implement the specified type.
 */
Service* App::lookupService(const string stype) {
    Service* s = (this->services);

    while (s != nullptr)  {
        if (this->type == stype) {
            return s;
        }
        s = (s->nextService);
    }
    return nullptr;
}


/**
 * Add a component to the application.
 * Return the new Component on success, null on failure.
 * NOTE: Does NOT match the Sedona method!
 */
Component& App::add(Component& cParent, Component& newComponent) {
	Component* sibling = lookup(cParent.children);
	
	if (sibling == nullptr) {
		// no children yet
		cParent.children = id;
	} else {
		// find last sibling
		while (sibling->nextSibling != nullId) {
			sibling = lookup(sibling->nextSibling);
		}
		sibling->nextSibling = id;
	}
	
	newComponent.id = comps.size();
	comps.push_back(newComponent);
	
	if ((this->running)) {
		cParent.childEvent((Component::ADDED), newComponent);
		newComponent.parentEvent((Component::ADDED), cParent);
		newComponent.loaded();
		newComponent.start();
	}
	cParent.fireTreeChanged();

	return *this;
}

/**
 * Allocate the next component id.  If no more space
 * is left in the comps array, then automatically
 * grow it.  Return -1 on error.
 */
int32_t App::allocCompId() {    
    return comps.size();
}


/**
 * Grow the comps array if too small.  Return true on success.
 */
bool App::ensureCompsCapacity(int32_t newLen) {
	// bad_alloc is thrown if the allocation request does not succeed
	comps.reserve(newLen);
    return true;
}


/**
 * Private implementation of adding a component into
 * the lookup tables - this method does NOT manage
 * the parent/child relationship.  Return true on
 * success, false on failure.
 */
bool App::insert(int32_t id, const Component* c) {
    /* FIXME */
    return true;
}


/**
 * Remove the specified component and free it's memory.
 * This method automatically recursively removes any children
 * of the component first.  Return true on success, false on
 * failure.
 */
bool App::remove(Component& c) {
	int32_t id;
	Component* parent;
	Component* kid;
	Link* link;

	parent = lookup(c.id);
	if (parent == nullptr) {
		return false;
	}

	// remove children of c
	kid = lookup(c.children);
	while (kid != nullptr) {
		Component* nextKid = lookup(kid->nextSibling);
		remove(*kid);
		kid = nextKid;
	}

	// remove c itself
	kid = lookup(parent->children);
	if (parent->children == c.id) {
		// c is first child -> unlink c
		parent->children = kid->nextSibling;
	} else {
		// c is not first child
		while (kid != nullptr && kid->nextSibling != c.id) {
			kid = lookup(kid->nextSibling);
		}
		// kid now points to predecessor of c -> unlink c
		kid->nextSibling = c.nextSibling;
	}

	link = (c.linksFrom);
	while (link != nullptr) {
		Link* next;

		next = (link->nextFrom);
		removeLink(link);
		link = next;
	}
	link = (c.linksTo);
	while (link != nullptr) {
		Link* next;

		next = (link->nextTo);
		removeLink(link);
		link = next;
	}
	if ((this->running)) {
		parent->childEvent((Component::REMOVED), c);
		c.parentEvent((Component::REMOVED), *parent);
	}
	parent->fireTreeChanged();

	return true;

}

/**
 * Gets first child component of a given type.
 * Returns null if no objects found, otherwise returns
 * a Component
 */
Component* App::getFirstChildOfType(const Component* parent, const string t) {
	/*
    Component* c;

    for (c = lookup((parent->children)); c != nullptr; c = lookup((c->nextSibling)))  {
        if (type->is(t)) {
            return c;
        }
    }*/
    return nullptr;
}


/**
 * Gets next sibling of a component that is of type t.
 * Returns null if end of sibling list is reached without finding one.
 */
Component* App::getNextSiblingOfType(const Component* component, const string t) {
	/*
    Component* c;

    if ((component->id) == (Component::nullId)) {
        return nullptr;
    }
    if ((component->nextSibling) == (Component::nullId)) {
        return nullptr;
    }
    for (c = lookup((component->nextSibling)); c != nullptr; c = lookup((c->nextSibling)))  {
        if (type->is(t)) {
            return c;
        }
    }
	*/
    return nullptr;
}


/**
 * Find a link with the specified from and to
 * ids or return null if not found.
 */
Link* App::lookupLink(int32_t fromCompId, int32_t fromSlotId, int32_t toCompId, int32_t toSlotId) {
    Component* to;

    to = lookup(toCompId);
    if (to != nullptr) {
        Link* x;

        for (x = (to->linksTo); x != nullptr; x = (x->nextTo))  {
            if (((x->toSlot) == toSlotId)&&((x->fromComp) == fromCompId)&&((x->fromSlot) == fromSlotId)) {
                return x;
            }
        }
    }
    return nullptr;
}


/**
 * Add a new Link into the application by registering it in both
 * the "to" and "from" component's linked-list of Links.  Return
 * the new Link or null on error.
 */
Link* App::addLink(Component* from, const Slot* fromSlot, Component* to, const Slot* toSlot) {
    Link* x;
    Link* link;

    if ((from == nullptr)||(fromSlot == nullptr)||(to == nullptr)||(toSlot == nullptr)) {
        return nullptr;
    }
    if (lookupLink((from->id), (fromSlot->id), (to->id), (toSlot->id)) != nullptr) {
        return nullptr;
    }
    for (x = (to->linksTo); x != nullptr; x = (x->nextTo))  {
        if ((x->toSlot) == (toSlot->id)) {
            return nullptr;
        }
    }
    link = new sys::Link;
    (link->fromComp) = static_cast<uint16_t>((from->id));
    (link->fromSlot) = static_cast<uint8_t>((fromSlot->id));
    (link->toComp) = static_cast<uint16_t>((to->id));
    (link->toSlot) = static_cast<uint8_t>((toSlot->id));
    insertLink(link);
    if ((this->running)) {
        to->linkEvent((Component::ADDED), link);
        from->linkEvent((Component::ADDED), link);
    }
    from->fireLinksChanged();
    to->fireLinksChanged();
    return link;
}


/**
 * Insert a Link into the application by registering it in both
 * the "to" and "from" component's list linked of Links.  Return
 * true on success, false on failure.
 */
bool App::insertLink(Link* link) {
    Component* from;
    Component* to;

    from = lookup((link->fromComp));
    to = lookup((link->toComp));
    if ((from == nullptr)||(to == nullptr)) {
        return false;
    }
    (link->nextTo) = (to->linksTo);
    (to->linksTo) = link;
    if (to != from) {
        (link->nextFrom) = (from->linksFrom);
        (from->linksFrom) = link;
    }
    return true;
}


/**
 * Remove a Link from the application by unregistering it from both
 * the "to" and "from" component's list linked of Links.  Return
 * true on success, false on failure.
 */
bool App::removeLink(const Link* link) {
    Component* from;
    Component* to;

    if (link == nullptr) {
        return false;
    }
    from = lookup((link->fromComp));
    if (from != nullptr) {
        Link* x;

        if ((from->linksFrom) == link) {
            (from->linksFrom) = (link->nextFrom);
        }
        for (x = (from->linksFrom); x != nullptr; x = (x->nextFrom))  {
            if ((x->nextFrom) == link) {
                (x->nextFrom) = (link->nextFrom);
                break;
            }
        }
    }
    to = lookup((link->toComp));
    if (to != nullptr) {
        Link* x;
        Slot* toSlot;

        if ((to->linksTo) == link) {
            (to->linksTo) = (link->nextTo);
        }
        for (x = (to->linksTo); x != nullptr; x = (x->nextTo))  {
            if ((x->nextTo) == link) {
                (x->nextTo) = (link->nextTo);
                break;
            }
        }
		/* TODO: 
        toSlot = type->slot((link->toSlot));
        if ((toSlot != nullptr)&&toSlot->isProp()) {
            to->setToDefault(toSlot);
        }*/
    }
    if ((this->running)) {
        from->linkEvent((Component::REMOVED), link);
        to->linkEvent((Component::REMOVED), link);
    }
    from->fireLinksChanged();
    to->fireLinksChanged();
    delete link;
    return (from != nullptr)&&(to != nullptr);
}


/**
 * Each service which uses watches, should call this
 * method on startup with it's service specific array
 * of Watch subclasses.
 */
void App::initWatches(const vector<Watch>& subclasses) {
    int32_t id;

    id = 0;
}


/**
 * Allocate a watch for a service using watches.  The service
 * should pass in its service specific array of Watch subclasses.
 * If a watch is opened then it is reserved, its closed field is
 * set to false, and the subclass instance is returned.  If all
 * the watches are currently open, then return null.
 */
Watch* App::openWatch(const vector<Watch>& subclasses) {
	/*
	TODO

    int32_t i;

    for (i = 0; i < (Watch::max); ++i)  {
        Watch* w;
        int32_t rand;
		        
        w = subclasses[i];
        (this->watches)[i] = w;
        (w->closed) = false;
        rand = Sys::rand() & 255;
        if ((w->rand) == rand) {
            ++rand;
        }
        (w->rand) = static_cast<uint8_t>(rand);
        return w;
    }
	*/
    return nullptr;
}


/**
 * Close the specific watch by freeing its id to be used
 * again and setting its closed field to false.
 */
void App::closeWatch(Watch* watch) {
    //(this->watches)[(watch->index)] = nullptr;
    (watch->closed) = true;
}


/**
 * Save the app in binary format to the output stream:
 * 
 *   app
 *   {
 *     u4           magic 0x73617070 "sapp"
 *     u4           version 0x0002 0.2
 *     Schema       schema
 *     Component[]  comps
 *     u2           0xffff end of comps marker
 *     Link[]       links
 *     u2           0xffff end of links marker
 *     u1           '.' end of app marker
 *   }
 * 
 * Return 0 on success, non-zero on failure.
 */
int32_t App::saveApp(OutStream* out) {
    out->writeI4(1935765616);
    out->writeI4(3);
    saveSchema(out);
    out->writeI2(maxId());
    out->writeI2(65535);
    out->writeI2(65535);
    return out->write(46) ? 0 : 1;
}


/**
 * Load the app from a binary format input stream.
 * Return 0 on success or non-zero on error.
 */
int32_t App::loadApp(InStream* in) {
    int32_t maxId;

    if (in->readS4() != 1935765616) {
        return (Err::invalidMagic);
    }
    if (in->readS4() != 3) {
        return (Err::invalidVersion);
    }
    if (!loadSchema(in)) {
        return (Err::invalidSchema);
    }
	/*
    maxId = in->readU2();
    initApp((maxId + 1));
    while (true)  {
        int32_t compId;
        Kit* kit;
        int32_t kitid;
        std::string t;
        Component* c;
        int32_t r;

        compId = in->readU2();
        if (compId == 65535) {
            break;
        }
        if (compId < 0) {
            return (Err::unexpectedEOF);
        }
        kit = nullptr;
        kitid = in->read();
        if (kitid >= 0) {
            kit = Sys::kit((this->kitIdMap)[kitid]);
        }
        if (kit == nullptr) {
            return (Err::invalidKitId);
        }
        t = kit->type(in->read());
		/* TODO
        if (t == nullptr) {
            return (Err::invalidTypeId);
        } 
        if (compId == 0) {
            c = this;
        }
         else  {
            c = new Component();
            if (c == nullptr) {
                return (Err::cannotMalloc);
            }
        }
        r = c->loadAppComp(in);
        if (r != 0) {
            return r;
        }
        if (c != this) {
            if (!insert(compId, c)) {
                return (Err::cannotInsert);
            }
        }
    }
    while (true)  {
        int32_t compId;
        Link* link;

        compId = in->readU2();
        if (compId == 65535) {
            break;
        }
        if (compId < 0) {
            return (Err::unexpectedEOF);
        }
        link = new sys::Link;
        if (link == nullptr) {
            return (Err::cannotMalloc);
        }
        if (!link->load(in, compId)) {
            return (Err::cannotLoadLink);
        }
        if (!insertLink(link)) {
            return (Err::cannotLoadLink);
        }
    }
    if (in->read() != 46) {
        return (Err::invalidAppEndMarker);
    }*/
    return 0;
}


/**
 * Schema is the list of kit names and checksums:
 * 
 *   schema
 *   {
 *     u1  count
 *     kits[count]
 *     {
 *       str  name
 *       u4   checksum
 *     }
 *   }
 */
void App::saveSchema(OutStream* out) {
    out->write((Sys::kitsLen));
}


/**
 * Check that the schema on the input stream matches
 * the schema of the current runtime.
 * 
 * In this version, we allow the app to depend on fewer kits than 
 * the scode, as long as all the app's kits are in the scode.
 */
bool App::loadSchema(InStream* in) {
	return false;
}


/**
 * Load the application from persistent storage
 */
int32_t App::load() {
	return (Err::cannotOpenFile);
	/*
    int32_t r;

	std::fstream fs;
	fs.open("test.txt", std::fstream::in);

    if (file->open("r")) {
        return (Err::cannotOpenFile);
    }
    r = loadApp(((this->file)->in));
    .file->close();
    return r;
	*/
}


/**
 * Save the application back to persistent storage
 */
void App::save() {
	/*
    if (.file->open("w")) {
        int32_t r;

        r = saveApp(((this->file)->out));
        .file->close();
        if (r == 0) {
            log.message("saved (" + .file->size() + " bytes)");
        }
         else  {
            log.error("save failed");
        }
    }*/
}


/**
 * Action to request hibernation.  Current execute loop will
 * complete and all services will get a chance to work before
 * hibernation occurs
 */
void App::hibernate() {
    (this->runStatus) = static_cast<uint8_t>((Err::hibernate));
}


/**
 * Save the application and then exit the main loop.
 */
void App::quit() {
    save();
    (this->running) = false;
}


/**
 * Action to invoke Platform.restart.
 */
void App::restart() {
    platform->restart();
    (this->running) = false;
    (this->runStatus) = static_cast<uint8_t>((Err::restart));
}


/**
 * Action to invoke Platform.reboot.
 */
void App::reboot() {
    platform->reboot();
}


/**
 * Copy constructor for 'App'
 */
/**
 * Move constructor for 'App'
 */
/**
 * Destructor
 */
App::~App() {}

Log App::log = Log();

const string App::TYPE_NAME = "sys::App";
const string App::BASE_TYPE_NAME = "sys::Component";

} // namespace sys
