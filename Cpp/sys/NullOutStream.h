/*********************************************************
 * Header file for class 'NullOutStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : NullOutStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_NULLOUTSTREAM_H
#define SYS_NULLOUTSTREAM_H

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
class NullOutStream;

/**
 * Implementation of OutStream which sinks to nowhere
 */
class NullOutStream : public OutStream {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        NullOutStream();

        /**
         * Virtual destructor
         */
        virtual ~NullOutStream();


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
    }; // class NullOutStream

} // namespace sys

#endif
