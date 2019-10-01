/*********************************************************
 * Header file for class 'NullInStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : NullInStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_NULLINSTREAM_H
#define SYS_NULLINSTREAM_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "InStream.h"


namespace sys {

// Forward declaration
class NullInStream;

/**
 * Implementation of InStream which can never be read
 */
class NullInStream : public InStream {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        NullInStream();

        /**
         * Virtual destructor
         */
        virtual ~NullInStream();


        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Method 'read' 
         * [OVERRIDE, PUBLIC]
         */
        int32_t read() override ;

        /**
         * Method 'readBytes' 
         * [OVERRIDE, PUBLIC]
         */
        int32_t readBytes(std::vector<uint8_t>& b, int32_t off, int32_t len) override ;

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
    }; // class NullInStream

} // namespace sys

#endif
