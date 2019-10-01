/*********************************************************
 * Implementation for class 'Service'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Service
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "Service.h"
#include "Component.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'Service 
 * _iInit
 * '
 */
Service::Service() {
    ;
    /* Init virt InitVirt sys::Service */;
    ;
}


/**
 * Perform a chunk of background work.  Return true
 * 
 * if there is pending work or false if the service is
 * 
 * done working this cycle.
 * 
 * 
 * 
 * A service should be designed to function correctly no
 * 
 * matter how many times work is called per execution cycle.
 * 
 * Returning false is not a guarantee that work will not be
 * 
 * called again in a given execution cycle; rather, it is
 * 
 * a hint to the App execution engine that this service does
 * 
 * not have any more work to do.
 * 
 * 
 * 
 * If you only want to do work once per execution cycle, you should consider:
 * 
 * 
 * 
 * 1
 *  Moving your work into the execute
 * 
 *  callback. execute
 * 
 *  will only be
 * 
 * called once per execution cycle.
 * 
 * 
 * 
 * 2
 *  Set a "newCycle" flag in your execute
 * 
 *  method and unset it after
 * 
 * doing one work cycle.  Only do your work if the newCycle flag is set.
 */
bool Service::work() {
    return false;
}


/**
 * Return true if this Service will allow hibernation.
 * 
 * Default is to return true, subclass must override
 * 
 * if it has a need to prevent hibernation.
 */
bool Service::canHibernate() {
    return true;
}


/**
 * Callback when device is entering low-power sleep mode.
 */
void Service::onHibernate() {
}


/**
 * Callback when device is exiting low-power sleep mode.
 */
void Service::onUnhibernate() {
}


/**
 * Copy constructor for 'Service'
 */
/**
 * Move constructor for 'Service'
 */
/**
 * Destructor
 */
Service::~Service() {}

const string Service::TYPE_NAME = "sys::Service";
const string Service::BASE_TYPE_NAME = "sys::Component";

} // namespace sys
