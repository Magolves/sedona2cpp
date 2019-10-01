/*********************************************************
 * Header file for class 'BufOutStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : BufOutStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_BUFOUTSTREAM_H
#define SYS_BUFOUTSTREAM_H

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
class BufOutStream;

/**
 * BufOutStream is used to write bytes to a memory Buf.
 */
class BufOutStream : public OutStream {

    //region Public members
    public:
        /**
         * Construct for specified Buf instance.
         */
        BufOutStream(Buf* buf);

        /**
         * Method '_iInit_OutStream' 
         * []
         */
        BufOutStream() = delete;

        /**
         * Virtual destructor
         */
        virtual ~BufOutStream();


        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Write a byte to the end of the buffer at buf[size].
         * Return true on success, false if buffer is full.
         */
        bool write(int32_t x) override ;

        /**
         * Write a block of bytes to the end of the buffer.
         * buffer size.  Return true on success, false if
         * there is not enough room in the buffer.
         */
        bool writeBytes(std::vector<uint8_t>& b, int32_t off, int32_t len) override ;

        /**
         * Write a block of bytes to the end of the buffer.
         * buffer size.  Return true on success, false if
         * there is not enough room in the buffer.
         */
        bool writeBytes(const uint8_t* b, int32_t off, int32_t len) ;

        Buf* buf;
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
    }; // class BufOutStream

} // namespace sys

#endif
