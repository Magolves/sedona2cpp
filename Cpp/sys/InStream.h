/*********************************************************
 * Header file for class 'InStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : InStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_INSTREAM_H
#define SYS_INSTREAM_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"


namespace sys {

// Forward declaration
class InStream;

/**
 * InStream is used to input text or binary data.
 */
class InStream {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        InStream();

        /**
         * Virtual destructor
         */
        virtual ~InStream();


        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Read an unsigned single byte. Return -1 if end
         * of stream is reached.
         */
        virtual int32_t read() = 0;

        /**
         * Read a block of bytes.  Return number of bytes
         * read into b, or -1 if end of stream.
         */
        virtual int32_t readBytes(std::vector<uint8_t>& b, int32_t off, int32_t len) = 0;

        /**
         * Close the stream.  Default implementation does nothing.
         */
        virtual void close();

        /**
         * Read a boolean value of zero or non-zero.
         */
        bool readBool();

        /**
         * Read a single character 
         * right now we only read ASCII
         * .
         */
        int32_t readChar();

        /**
         * Read a null terminated string into a buffer.
         * Return true on success, false on error or end
         * of stream.
         */
        bool readStr(std::string& s, int32_t max);

        /**
         * Read an unsigned two byte integer in network
         * byte order.  Return -1 if end of stream.
         */
        int32_t readU2();

        /**
         * Read an signed four byte 
         * 32-bit
         *  integer in network
         * byte order.  Return -1 if end of stream.
         */
        int32_t readS4();

        /**
         * Read an signed eight byte 
         * 64-bit
         *  integer in network
         * byte order.  Return -1 if end of stream.
         */
        int64_t readS8();

        /**
         * Read a four byte 
         * 32-bit
         *  floating point value in network byte order
         */
        float readF4();

        /**
         * Read a eight byte 
         * 64-bit
         *  floating point value in network byte order
         */
        double readF8();

        /**
         * Skip n number of bytes.
         */
        void skip(int32_t n);

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
    }; // class InStream

} // namespace sys

#endif
