/*********************************************************
 * Implementation for class 'PlatformService'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : PlatformService
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifdef _WIN32
//#include <sysinfoapi.h>
#include <windows.h>
#else
#endif


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "PlatformService.h"
#include "Config.h"
#include "Slot.h"
#include "Buf.h"
#include "App.h"
#include "Sys.h"
#include "Service.h"


// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'PlatformService 
 * _iInit
 * '
 */
PlatformService::PlatformService() {

    memAvailable = 0;    
    platformId= "unknown";    
	platformVer = "0";
}


/**
 * Lookup the platform service for this VM.  Return null
 * if the application hasn't booted yet or is missing
 * a platform service.
 */
PlatformService* PlatformService::lookup() {
    return static_cast<PlatformService*>(Sys::app.lookupService("sys::PlatformService"));	
}


/**
 * Initialize the platform with the command line arguments.
 * Return 0 on success.  Return  non-zero on failure, which will
 * terminate the process.
 */
int32_t PlatformService::init(const vector<string>& args, int32_t argsLen) {
    return 0;
}


/**
 * Notify the platform of a name/value pair setting
 * or state change.
 */
void PlatformService::notify(const string key, const string val) {
}


/**
 * Convenience to set the platformId property, and call changed
 */
void PlatformService::updatePlatformId(const string id) {
	platformId = id;
    changed(platformId);
}


/**
 * Convenience to set the platformVer property, and call changed
 */
void PlatformService::updatePlatformVer(const string ver) {
	platformVer = ver;
    changed(platformVer);
}


/**
 * Implementation of method 'start'
 */
void PlatformService::start() {
    start();
    refreshMemory();
    updatePlatformId(doPlatformId());
    updatePlatformVer(getPlatVersion());
}


/**
 * Implementation of method 'execute'
 */
void PlatformService::execute() {
    refreshMemory();
}


/**
 * By default, a Platform Service assumes that the platform does not
 * support hibernation, so this method always returns false.  If your
 * platform supports hibernation, you should override this method to
 * return true.
 */
bool PlatformService::canHibernate() {
    return false;
}


/**
 * Update the memAvailable property to reflect the current
 * amount of available memory.
 */
void PlatformService::refreshMemory() {
    setLong(memAvailable, getNativeMemAvailable());
}


/**
 * Return true if App must yield CPU by exiting VM each cycle.
 * 
 * Platforms that support pre-emptive multitasking should return false
 * since App can sleep to yield CPU.
 * 
 * Cooperative multitasking platforms that expect tasks to run to
 * completion and return so other tasks can execute should return true.
 * 
 * See also the Apps chapter in Sedona docs
 */
bool PlatformService::yieldRequired() {
    return false;
}


/**
 * Inform platform that SVM will be exiting with Err.yield and needs
 * to be scheduled to run again 
 * via resume in vm.c
 *  in ns nanoseconds to meet
 * the desired App.scanPeriod value.
 * 
 * If the application overruns its scan period, yieldTime will be negative.
 * In this case the VM should be scheduled to run again as soon as possible.
 * 
 * See also the Apps chapter in Sedona docs
 */
void PlatformService::yield(int64_t yieldTime) {
}


/**
 * Returns true if the platform wants to the App to invoke work
 * again on all Services during free time before the next App execute needs to begin.
 * 
 * For Apps with long scanPeriods greater than 50ms, this allows Services 
 * to respond to network traffic or other asynchronous events multiple times 
 * during a single scanPeriod - even if the service didn't have any immediate 
 * work to do.  
 * 
 * i.e. - Sox services responds to a poll request, but then has no further
 * work, nor doing any other services.  A few ms later, another poll request 
 * comes in.  If this method returns false, no further workd will be done 
 * until the beginning of the next scan period  If this method returns true, 
 * work
 * 
 *  will be called on all services again and the poll request will
 * be handled before the next scanPeriod.  
 * 
 * nsUntilDeadline represents the time remaining before the next App 
 * execute
 * 
 *  cycle again.  Depending on the underlying OS/system 
 * architecture, the platform may want to sleep or otherwise relinquish the 
 * CPU for some fraction of nsUntilDeadline before returning and
 * allowing App to call service work again.
 * 
 * See also the Apps chapter in Sedona docs
 */
bool PlatformService::workDuringFreeTime(int64_t nsUntilDeadline) {
    return false;
}


/**
 * Return the current state of the network interface.  For devices
 * that support multiple interfaces, return the state of the primary
 * network interface on the platform.
 */
int32_t PlatformService::getNetworkState() {
    return (PlatformService::netOk);
}


/**
 * Action to restart the Sedona application on the host platform.
 * This typically means restart just the Sedona process.
 */
void PlatformService::restart() {
}


/**
 * Action to reboot the entire host platform.  This typically
 * means to reboot the entire operating system.
 */
void PlatformService::reboot() {
}


/**
 * Get the platform identifier which defines how
 * this Sedona device should be provisioned.
 */
string PlatformService::doPlatformId()/* / Native or external function call */
{ 
	return Sys::platformType();
} 


/**
 * Get the platform SVM version.
 */
string PlatformService::getPlatVersion()/* / Native or external function call */
{ 
	// FIXME
	return "latest & greatest";
} 


/**
 * Returns number of bytes available on the native platform.
 */
int64_t PlatformService::getNativeMemAvailable()/* / Native or external function call */
{ 
#ifdef __MINGW32__
	return 12345;
#else
#ifdef _WIN32
		MEMORYSTATUSEX status;
		status.dwLength = sizeof(status);
		GlobalMemoryStatusEx(&status);
		return status.ullTotalPhys;

		return 123456L;
#elif _WIN64
		MEMORYSTATUSEX status;
		status.dwLength = sizeof(status);
		GlobalMemoryStatusEx(&status);
		return status.ullTotalPhys;
#else
		long pages = sysconf(_SC_PHYS_PAGES);
		long page_size = sysconf(_SC_PAGE_SIZE);
		return pages * page_size;
#endif
#endif
	
} 


/**
 * Copy constructor for 'PlatformService'
 */
/**
 * Move constructor for 'PlatformService'
 */
/**
 * Destructor
 */
PlatformService::~PlatformService() {}

const int32_t PlatformService::netConfigError = -2;
const int32_t PlatformService::netFatal = -1;
const int32_t PlatformService::netInitializing = 1;
const int32_t PlatformService::netOk = 0;
const string PlatformService::TYPE_NAME = "sys::PlatformService";
const string PlatformService::BASE_TYPE_NAME = "sys::Service";

} // namespace sys
