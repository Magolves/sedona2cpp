/*********************************************************
 * Implementation for class 'Sys'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Sys
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

/* STL includes */
#include <stdint.h>
#include <chrono>
#include <fstream>
#include <iostream>
#include <iterator>
#include <random>
#include <sstream>
#include <thread>
#include <vector>

#include "App.h"
#include "Config.h"
#include "Err.h"
#include "Kit.h"
#include "Log.h"
#include "NullInStream.h"
#include "NullOutStream.h"
#include "OutStream.h"
#include "StdOutStream.h"
#include "Sys.h"
#include "Test.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;
using namespace chrono;

namespace sys {

/**
 * Constructor for 'Sys
 * _iInit
 * '
 */
Sys::Sys() {}

/**
 * Get the qualified name of the PlatformService type which is
 * required by this hardware/OS platform.  There must be exactly
 * one PlatformService defined in the App which implements this
 * type in order to boot the system.
 */
string Sys::platformType() /* / Native or external function call */
{
#ifdef _WIN32
  return "Windows 32-bit";
#elif _WIN64
  return "Windows 64-bit";
#elif __APPLE__ || __MACH__
  return "Mac OSX";
#elif __linux__
  struct utsname uts;
  uname(&uts);
  return "Linux " + uts.sysname;
#elif __FreeBSD__
  return "FreeBSD";
#elif __unix || __unix__
  struct utsname uts;
  uname(&uts);
  return "Unix " + uts.sysname;
#else
  return "Other";
#endif
}

/**
 * Get the log for the specified id or return null if out of range.
 */
Log* Sys::log(int32_t id) {
  if ((0 <= id) && (id < (Sys::logsLen))) {
    return &(Sys::logs)[id];
  }
  return nullptr;
}

/**
 * Find a log by its qualified name or return null if not found.
 */
Log* Sys::findLog(const string qname) {
  size_t i = 0;
  while (i < logs.size()) {
    if (logs[i++].qname == qname) {
      return &logs[i];
    }
  }

  return nullptr;
}

/**
 * Get the current time as a 64-bit integer of nanosecond ticks.
 * Ticks are not necessarily based on wall-time, but rather based
 * on an arbitrary epoch
 * typically the boot time of the host
 */
int64_t Sys::ticks() /* / Native or external function call */
{
  TimeStamp<Ticks> currentTime = time_point_cast<Ticks>(Clock::now());
  return (currentTime - start_).count();
}

/**
 * Sleep for the specified time in nanosecond ticks.
 * If t <= 0, then sleep should return immediately.
 *
 * This method is for use by App and test code only.  Do not call from
 * within Component execute
 *
 *  or Service work
 *
 *  methods.
 */
void Sys::sleep(int64_t t) /* / Native or external function call */
{
  this_thread::sleep_for(Ticks(t));
}

/**
 * This is the entry point where native code resumes the
 * Sedona VM from a yield or hibernate state.  VM heap
 * remains intact.
 */
int32_t Sys::resume(const vector<string>&, int32_t) {
  return Sys::shutdown(app.resumeApp());
}

/**
 * This method unwinds the Sedona call stack and the VM exits
 * with the given error code.
 *
 * If it returns 'Err.hibernate' or 'Err.yield', the Sedona heap
 * remains intact and can be restarted via 'resume' entry point
 *
 * If the specified result is not the hibernate or yield error
 * codes, then stop the application and cleanup memory.
 */
int32_t Sys::shutdown(int32_t result) {
  if (result == Err::yield || result == Err::hibernate) {
  } else {
    app.stopApp();
    app.cleanupApp();
  }
  return result;
}

/**
 * Copy num bytes from the source byte array to the destination byte
 * array.  The arrays may be overlapping
 * like memmove, not memcpy
 * .
 */
void Sys::copy(const uint8_t* src, int32_t srcOff, uint8_t* dest,
               int32_t destOff, int32_t num) {
  memcpy(dest + destOff, src + srcOff, num);
}

/**
 * Compare two byte arrays for equality. If equal return 0, if
 * a is less than b return -1, if a greater than b return 1.
 */
int32_t Sys::compareBytes(vector<uint8_t>& a, int32_t aOff, vector<uint8_t>& b,
                          int32_t bOff,
                          int32_t len) /* / Native or external function call */
{
  return compareBytes(&a[0], aOff, &b[0], bOff, len);
}

/**
 * Compare two byte arrays for equality. If equal return 0, if
 * a is less than b return -1, if a greater than b return 1.
 */
int32_t Sys::compareBytes(const uint8_t* a, int32_t aOff, const uint8_t* b,
                            int32_t bOff, int32_t len) {
  for (auto i = 0; i < len; i++) {
    if (a[aOff + i] != b[bOff + i]) {
      return a[aOff + i] < b[bOff + i] ? -1 : 1;
    }
  }

  return 0;
}

/**
 * Set all the bytes in the specified array to val.
 */
void Sys::setBytes(int32_t val, vector<uint8_t>& bytes, int32_t off,
                   int32_t len) /* / Native or external function call */
{
  for (auto i = 0; i < len; i++) {
    bytes[off + i] = val;
  }
}

/**
 * Perform a bitwise "and" using the specified mask on each
 * byte in the bytes array.
 */
void Sys::andBytes(int32_t mask, vector<uint8_t>& bytes, int32_t off,
                   int32_t len) /* / Native or external function call */
{
  for (auto i = 0; i < len; i++) {
    bytes[off + i] &= mask;
  }
}

/**
 * Perform a bitwise "or" using the specified mask on each
 * byte in the bytes array.
 */
void Sys::orBytes(int32_t mask, vector<uint8_t>& bytes, int32_t off,
                  int32_t len) /* / Native or external function call */
{
  for (auto i = 0; i < len; i++) {
    bytes[off + i] |= mask;
  }
}

/**
 * Get the base address of the scode image as a byte pointer.
 * INTERNAL USE ONLY.
 */
vector<uint8_t>& Sys::scodeAddr() /* / Native or external function call */
{
  throw "scodeAddr() not supported";
}

/**
 * Format an integer as a decimal string.
 * The string is stored in a static shared buffer.
 */
string Sys::intStr(int32_t v) /* / Native or external function call */
{
  return to_string(v);
}

/**
 * Format an integer as a hexadecimal string.
 * The string is stored in a static shared buffer.
 */
string Sys::hexStr(int32_t v) /* / Native or external function call */
{
  stringstream ss;
  ss << std::hex << v;
  return ss.str();
}

/**
 * Format the 64-bit integer into a string.
 * The string is stored in a static shared buffer.
 */
string Sys::longStr(int64_t v) /* / Native or external function call */
{
  return to_string(v);
}

/**
 * Format the 64-bit integer into a hexidecimal string.
 * The string is stored in a static shared buffer.
 */
string Sys::longHexStr(int64_t v) /* / Native or external function call */
{
  stringstream ss;
  ss << std::hex << v;
  return ss.str();
}

/**
 * Format a float into a string.
 * The string is stored in a static shared buffer.
 */
string Sys::floatStr(float v) /* / Native or external function call */
{
  return to_string(v);
}

/**
 * Format a double into a string.
 * The string is stored in a static shared buffer.
 */
string Sys::doubleStr(double v) /* / Native or external function call */
{
  return to_string(v);
}

/**
 * Return an integer representation of a 32-bit floating point
 * value according to the IEEE 754 floating-point "single format"
 * bit layout.
 */
int32_t Sys::floatToBits(float v) /* / Native or external function call */
{
  return *((int32_t*)&v);
}

/**
 * Return a long representation of a 64-bit floating point
 * value according to the IEEE 754 floating-point "double format"
 * bit layout.
 */
int64_t Sys::doubleToBits(double v) /* / Native or external function call */
{
  return *((int64_t*)&v);
}

/**
 * Return a 32-bit floating point value according to the
 * IEEE 754 floating-point "single format" bit layout.
 */
float Sys::bitsToFloat(int32_t bits) /* / Native or external function call */
{
  return *((float*)&bits);
}

/**
 * Return a 64-bit floating point value according to the
 * IEEE 754 floating-point "double format" bit layout.
 */
double Sys::bitsToDouble(int64_t bits) /* / Native or external function call */
{
  return *((double*)&bits);
}

/**
 * Return the numeric value of a decimal character:
 *   '0' -> 0
 *   '9' -> 9
 * Return -1 if not a valid decimal digit.
 */
int32_t Sys::fromDigit(int32_t digit) {
  if ((48 <= digit) && (digit <= 57)) {
    return digit - 48;
  }
  return -1;
}

/**
 * Return the numeric value of an decimal character:
 *   '0' -> 0
 *   '9' -> 9
 *   'a' or 'A' -> 10
 *   'f' or 'F' -> 15
 * Return -1 if not a valid hex digit.
 */
int32_t Sys::fromHexDigit(int32_t digit) {
  if ((48 <= digit) && (digit <= 57)) {
    return digit - 48;
  }
  if ((97 <= digit) && (digit <= 102)) {
    return (digit - 97) + 10;
  }
  if ((65 <= digit) && (digit <= 70)) {
    return (digit - 65) + 10;
  }
  return -1;
}

/**
 * Return the max of two ints.
 
int32_t Sys::max(int32_t a, int32_t b) {
    return (a > b) ? a : b;
}

/**
 * Return the min of two ints.
 
int32_t Sys::min(int32_t a, int32_t b) {
    return (a < b) ? a : b;
}*/

/**
 * Generate a random 32-bit integer.
 */
int32_t Sys::rand() /* / Native or external function call */
{
  return std::rand();
}

/**
 * Print version information to the output stream.
 */
void Sys::printVer(OutStream* out) { out->print("0.07"); }

/**
 * Standard main entry point.
 */
int32_t Sys::main(App& app, const vector<string>& args, int32_t argsLen) {
  std::string appFile;
  int32_t r;

  (Sys::bootTime) = Sys::ticks();
  Sys::setBytes((Log::MESSAGE), (Sys::logLevels), 0, (Sys::logsLen));
  /*
appFile = nullptr;
if (appFile == nullptr) {
  return (Err::invalidArgs);
}
((((&Sys::app))->file)->name) = appFile;
r = app.load();
if (r != 0) {
  nl();
  return r;
}*/

  Sys::app = app;
  r = app.startApp(args, argsLen);

  if (r != 0) {
    out.nl();
    return r;
  }
  return Sys::shutdown(app.runApp());
}

App Sys::app = App() /* DEF */;
int64_t Sys::bootTime = 0L /* DEF */;
vector<Kit> Sys::kits = {} /* DEF */;
const int32_t Sys::kitsLen = 1;
vector<uint8_t> Sys::logLevels = {} /* DEF */;
vector<Log> Sys::logs = {} /* DEF */;
const int32_t Sys::logsLen = 1;
const int32_t Sys::maxInt = 2147483647;
const int64_t Sys::maxLong = 9223372036854775807l;
const int32_t Sys::minInt = -2147483648;
const int64_t Sys::minLong = 0x8000000000000000l;
NullInStream Sys::nullIn = NullInStream() /* DEF */;
NullOutStream Sys::nullOut = NullOutStream() /* DEF */;
StdOutStream Sys::out = StdOutStream() /* DEF */;
const int32_t Sys::sizeofRef = 0;

TimeStamp<Ticks> Sys::start_ = time_point_cast<Ticks>(Clock::now());
}  // namespace sys
