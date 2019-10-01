/*********************************************************
 * Header file for class 'BufInStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : BufInStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_BUFINSTREAM_H
#define SYS_BUFINSTREAM_H

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
#include "Buf.h"


namespace sys {

// Forward declaration
class BufInStream;

/**
 * BufInStream is used to read bytes from a memory Buf.
 */
class BufInStream : public InStream {

    //region Public members
    public:
        /**
         * Construct for specified Buf instance.
         */
        BufInStream(Buf* buf);

        /**
         * Method '_iInit_InStream' 
         * []
         */
        BufInStream();

        /**
         * Virtual destructor
         */
        virtual ~BufInStream();


        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Read the next byte at pos and increment pos.
         * Return -1 if pos >= size.
         */
        int32_t read() override ;

        /**
         * Read the next block of available bytes starting from pos.
         * Return the number of bytes copied into b and advance
         * the position likewise.
         */
        int32_t readBytes(std::vector<uint8_t>& b, int32_t off, int32_t len) override ;

        /**
         * Read the next block of available bytes starting from pos.
         * Return the number of bytes copied into b and advance
         * the position likewise.
         */
        int32_t BufInStream::readBytes(uint8_t* b, int32_t off, int32_t len);

        /**
         * Read the current position as a null terminated
         * string.  Return null on error.
         */
        std::string readStrInPlace(const std::string str);

        /**
         * Rewind sets the pos back to zero to begin
         * reading the Buf from the beginning.
         */
        void rewind();

        Buf* buf;
        uint16_t pos = 0 /* DEF */;
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
    }; // class BufInStream

} // namespace sys

#endif
