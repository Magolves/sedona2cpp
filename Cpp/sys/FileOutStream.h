/*********************************************************
 * Header file for class 'FileOutStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : FileOutStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_FILEOUTSTREAM_H
#define SYS_FILEOUTSTREAM_H

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
#include "Buf.h"


namespace sys {

// Forward declaration
class FileOutStream;

/**
 * FileOutStream
 */
class FileOutStream : public OutStream {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        FileOutStream();

        /**
         * Virtual destructor
         */
        virtual ~FileOutStream();


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
         * Method 'close' 
         * [OVERRIDE, PUBLIC]
         */
        void close() override ;

        Buf fbuf;
        //endregion
    //endregion
    protected:

    //region Protected members/methods
		/**
		 * Method 'writeBufToFile'
		 * [INTERNAL]
		 */
		bool writeBufToFile();

        //region Fields
        std::ofstream file;
        //endregion
    //endregion

    //region Private members/methods
    private:

        //region Fields
        //endregion
    //endregion
    }; // class FileOutStream

} // namespace sys

#endif
