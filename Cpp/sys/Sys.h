/*********************************************************
 * Header file for class 'Sys'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Sys
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_SYS_H
#define SYS_SYS_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "Config.h"
#include "Log.h"
#include "OutStream.h"
#include "App.h"
#include "NullInStream.h"
#include "StdOutStream.h"
#include "Kit.h"
#include "NullOutStream.h"


namespace sys {

// Forward declaration
class Sys;

/**
 * Sys provides access to the system environment.
 */
class Sys {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        Sys();


        static const int32_t kitsLen;
        static const int32_t logsLen;
        static const int32_t maxInt;
        static const int64_t maxLong;
        static const int32_t minInt;
        static const int64_t minLong;
        static const int32_t sizeofRef;

        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Get the qualified name of the PlatformService type which is
         * required by this hardware/OS platform.  There must be exactly
         * one PlatformService defined in the App which implements this
         * type in order to boot the system.
         */
        static std::string platformType();

        /**
         * Get the kit for the specified id or return null if out of range.
         */
        //static Kit* kit(int32_t id);

        /**
         * Get the type for the specified kit and type id or
         * return null if out of range.
         */
        static std::string type(int32_t kitId, int32_t typeId);

        /**
         * Find a kit by its unique name or return null if not found.
         */
        //static Kit* findKit(const std::string name);

        /**
         * Find a type by its qualified name such as 
         * "sys::Component" or return null if not found.
         */
        //static std::string findType(const std::string qname);

        /**
         * Get the log for the specified id or return null if out of range.
         */
        static Log* log(int32_t id);

        /**
         * Find a log by its qualified name or return null if not found.
         */
        static Log* findLog(const std::string qname);

        /**
         * Get the current time as a 64-bit integer of nanosecond ticks.  
         * Ticks are not necessarily based on wall-time, but rather based 
         * on an arbitrary epoch 
         * typically the boot time of the host
         */
        static int64_t ticks();

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
        static void sleep(int64_t t);

        /**
         * This is the entry point where native code resumes the 
         * Sedona VM from a yield or hibernate state.  VM heap
         * remains intact.
         */
        static int32_t resume(const std::vector<std::string>& args, int32_t argsLen);

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
        static int32_t shutdown(int32_t result);
		
        /**
         * Copy num bytes from the source byte array to the destination byte
         * array.  The arrays may be overlapping 
         * like memmove, not memcpy
         * .
         */
        static void copy(const uint8_t* src, int32_t srcOff, uint8_t* dest, int32_t destOff, int32_t num);

        /**
         * Compare two byte arrays for equality. If equal return 0, if
         * a is less than b return -1, if a greater than b return 1.
         */
        static int32_t compareBytes(std::vector<uint8_t>& a, int32_t aOff, std::vector<uint8_t>& b, int32_t bOff, int32_t len);

        /**
         * Compare two byte arrays for equality. If equal return 0, if
         * a is less than b return -1, if a greater than b return 1.
         */
        static int32_t compareBytes(const uint8_t* a, int32_t aOff,
                                    const uint8_t* b, int32_t bOff,
                                    int32_t len);

        /**
         * Set all the bytes in the specified array to val.
         */
        static void setBytes(int32_t val, std::vector<uint8_t>& bytes, int32_t off, int32_t len);

        /**
         * Perform a bitwise "and" using the specified mask on each
         * byte in the bytes array.
         */
        static void andBytes(int32_t mask, std::vector<uint8_t>& bytes, int32_t off, int32_t len);

        /**
         * Perform a bitwise "or" using the specified mask on each
         * byte in the bytes array.
         */
        static void orBytes(int32_t mask, std::vector<uint8_t>& bytes, int32_t off, int32_t len);

        /**
         * Get the base address of the scode image as a byte pointer.
         * INTERNAL USE ONLY.
         */
        static std::vector<uint8_t>& scodeAddr();

        /**
         * Format an integer as a decimal string.
         * The string is stored in a static shared buffer.
         */
        static std::string intStr(int32_t v);

        /**
         * Format an integer as a hexadecimal string.
         * The string is stored in a static shared buffer.
         */
        static std::string hexStr(int32_t v);

        /**
         * Format the 64-bit integer into a string.
         * The string is stored in a static shared buffer.
         */
        static std::string longStr(int64_t v);

        /**
         * Format the 64-bit integer into a hexidecimal string.
         * The string is stored in a static shared buffer.
         */
        static std::string longHexStr(int64_t v);

        /**
         * Format a float into a string.
         * The string is stored in a static shared buffer.
         */
        static std::string floatStr(float v);

        /**
         * Format a double into a string.
         * The string is stored in a static shared buffer.
         */
        static std::string doubleStr(double v);

        /**
         * Return an integer representation of a 32-bit floating point
         * value according to the IEEE 754 floating-point "single format"
         * bit layout.
         */
        static int32_t floatToBits(float v);

        /**
         * Return a long representation of a 64-bit floating point
         * value according to the IEEE 754 floating-point "double format"
         * bit layout.
         */
        static int64_t doubleToBits(double v);

        /**
         * Return a 32-bit floating point value according to the
         * IEEE 754 floating-point "single format" bit layout.
         */
        static float bitsToFloat(int32_t bits);

        /**
         * Return a 64-bit floating point value according to the
         * IEEE 754 floating-point "double format" bit layout.
         */
        static double bitsToDouble(int64_t bits);

        /**
         * Return the numeric value of a decimal character:
         *   '0' -> 0
         *   '9' -> 9
         * Return -1 if not a valid decimal digit.
         */
        static int32_t fromDigit(int32_t digit);

        /**
         * Return the numeric value of an decimal character:
         *   '0' -> 0
         *   '9' -> 9
         *   'a' or 'A' -> 10
         *   'f' or 'F' -> 15
         * Return -1 if not a valid hex digit.
         */
        static int32_t fromHexDigit(int32_t digit);

        /**
         * Return the max of two ints.
         */
		// Name clash with existing macro?!
        //static int32_t max(int32_t a, int32_t b);

        /**
         * Return the min of two ints.
         */
		 // Name clash with existing macro?!
        //static int32_t min(int32_t a, int32_t b);

        /**
         * Generate a random 32-bit integer.
         */
        static int32_t rand();

        /**
         * Print version information to the output stream.
         */
        static void printVer(OutStream* out);

        /**
         * Standard main entry point.
         */
        static int32_t main(App& app, const std::vector<std::string>& args, int32_t argsLen);

		
		static App app;
        static int64_t bootTime;
        static std::vector<uint8_t> logLevels;
        static NullInStream nullIn;
        static NullOutStream nullOut;
        static StdOutStream out;
        static std::vector<Kit> kits;
        static std::vector<Log> logs;
        //endregion
    //endregion
    protected:

    //region Protected members/methods

        //region Fields
        //endregion
    //endregion

    //region Private members/methods
    private:

        //region Fields
		static TimeStamp<Ticks> start_;
        //endregion
    //endregion
    }; // class Sys

} // namespace sys

#endif
