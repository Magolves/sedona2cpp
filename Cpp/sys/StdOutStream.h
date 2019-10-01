/*********************************************************
 * Header file for class 'StdOutStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : StdOutStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_STDOUTSTREAM_H
#define SYS_STDOUTSTREAM_H

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
class StdOutStream;

/**
 * Implementation of OutStream which sinks to standard output.
 */
class StdOutStream : public OutStream {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        StdOutStream();

        /**
         * Virtual destructor
         */
        virtual ~StdOutStream();


        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Method 'write' 
         * [OVERRIDE, PUBLIC]
         */
        bool write(int32_t b) override ;

        /**
         * Method 'writeBytes' 
         * [OVERRIDE, PUBLIC]
         */
        bool writeBytes(std::vector<uint8_t>& b, int32_t off, int32_t len) override ;

        /**
         * Method 'flush' 
         * [OVERRIDE, PUBLIC]
         */
        void flush() override ;

        /**
         * Method 'doWrite' 
         * [NATIVE, PUBLIC, STATIC]
         */
        static bool doWrite(int32_t b);

        /**
         * Method 'doWriteBytes' 
         * [NATIVE, PUBLIC, STATIC]
         */
        static bool doWriteBytes(std::vector<uint8_t>& b, int32_t off, int32_t len);

        /**
         * Method 'doFlush' 
         * [NATIVE, PUBLIC, STATIC]
         */
        static void doFlush();

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
        //endregion
    //endregion
    }; // class StdOutStream

} // namespace sys

#endif
