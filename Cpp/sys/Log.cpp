/*********************************************************
 * Implementation for class 'Log'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Log
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "Log.h"
#include "Sys.h"
#include "OutStream.h"
#include "StdOutStream.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'Log 
 * _iInit
 * '
 */
Log::Log() {
}


/**
 * Get the currently configured severity level
 * enabled for this Log.  The level is NONE, ERROR,
 * WARNING, MESSAGE, and TRACE.
 */
int32_t Log::level() {
    return (Sys::logLevels)[(this->id)];
}


/**
 * Set the severity level for this Log.  
 * Possible levels are NONE, ERROR, WARNING, MESSAGE, 
 * and TRACE.  
 * no error checking on arg
 */
void Log::setLevel(int32_t newLevel) {
    (Sys::logLevels)[(this->id)] = static_cast<uint8_t>(newLevel);
}


/**
 * Is current level error or greater.
 */
bool Log::isError() {
    return (Sys::logLevels)[(this->id)] >= (Log::ERR);
}


/**
 * Is current level warning or greater.
 */
bool Log::isWarning() {
    return (Sys::logLevels)[(this->id)] >= (Log::WARNING);
}


/**
 * Is current level message or greater.
 */
bool Log::isMessage() {
    return (Sys::logLevels)[(this->id)] >= (Log::MESSAGE);
}


/**
 * Is current level trace or greater.
 */
bool Log::isTrace() {
    return (Sys::logLevels)[(this->id)] >= (Log::TRACE);
}


/**
 * Log an error record.
 */
OutStream* Log::error(const string msg) {
    return log("ERROR", msg);
}


/**
 * Log a warning record.
 */
OutStream* Log::warning(const string msg) {
    return log("WARNING", msg);
}


/**
 * Log a message record.
 */
OutStream* Log::message(const string msg) {
    return log("MESSAGE", msg);
}


/**
 * Log a trace record.
 */
OutStream* Log::trace(const string msg) {
    return log("TRACE", msg);
}


/**
 * Implementation of method 'log'
 */
OutStream* Log::log(const string level, const string msg) {
	Sys::out.print("-- ")->print(level)->print(" [")->print(this->qname)->print("] ")->print(msg)->nl();
	return &Sys::out;
}


} // namespace sys
