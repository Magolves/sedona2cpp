/*********************************************************
 * Header file for class 'FileInStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : FileInStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_FILEINSTREAM_H
#define SYS_FILEINSTREAM_H

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
class FileInStream;

/**
 * FileInStream
 */
class FileInStream : public InStream {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        FileInStream();

        /**
         * Virtual destructor
         */
        virtual ~FileInStream();


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

        /**
         * Method 'close' 
         * [OVERRIDE, PUBLIC]
         */
        void close() override ;

        //endregion
    //endregion
    protected:

    //region Protected members/methods

        //region Fields
        std::fstream* file;
        //endregion
    //endregion

    //region Private members/methods
    private:

        //region Fields
        //endregion
    //endregion
    }; // class FileInStream

} // namespace sys

#endif
