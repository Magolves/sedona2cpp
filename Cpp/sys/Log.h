/*********************************************************
 * Header file for class 'Log'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Log
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_LOG_H
#define SYS_LOG_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "OutStream.h"


namespace sys {

// Forward declaration
class Log;

/**
 * Log is used to embed error, warning, message, and
trace logging into Sedona software.
 */
class Log {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        Log();
		
		
        static const int32_t MESSAGE = 3;
        static const int32_t NONE = 0;
		static const int32_t ERR = 1; // renamed to ERR due to name clash with windows.h
        static const int32_t TRACE = 4;
        static const int32_t WARNING = 2;
        uint16_t id = 0 /* DEF */;

        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Get the currently configured severity level
         * enabled for this Log.  The level is NONE, ERROR,
         * WARNING, MESSAGE, and TRACE.
         */
        int32_t level();

        /**
         * Set the severity level for this Log.  
         * Possible levels are NONE, ERROR, WARNING, MESSAGE, 
         * and TRACE.  
         * no error checking on arg
         */
        void setLevel(int32_t newLevel);

        /**
         * Is current level error or greater.
         */
        bool isError();

        /**
         * Is current level warning or greater.
         */
        bool isWarning();

        /**
         * Is current level message or greater.
         */
        bool isMessage();

        /**
         * Is current level trace or greater.
         */
        bool isTrace();

        /**
         * Log an error record.
         */
        OutStream* error(const std::string msg);

        /**
         * Log a warning record.
         */
        OutStream* warning(const std::string msg);

        /**
         * Log a message record.
         */
        OutStream* message(const std::string msg);

        /**
         * Log a trace record.
         */
        OutStream* trace(const std::string msg);

        std::string qname;
        //endregion
    //endregion
    protected:

    //region Protected members/methods

        //region Fields
        //endregion
    //endregion

    //region Private members/methods
    private:
        /**
         * Method 'log' 
         * [PRIVATE]
         */
        OutStream* log(const std::string level, const std::string msg);


        //region Fields
        //endregion
    //endregion
    }; // class Log

} // namespace sys

#endif
