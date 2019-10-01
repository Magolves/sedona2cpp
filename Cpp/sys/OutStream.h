/*********************************************************
 * Header file for class 'OutStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : OutStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_OUTSTREAM_H
#define SYS_OUTSTREAM_H

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
class OutStream;

/**
 * OutStream is used to output printed text or binary encoded data.
 */
class OutStream {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        OutStream();

        /**
         * Virtual destructor
         */
        virtual ~OutStream();


        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Write a single byte.
         * Return true on success, false on failure.
         */
        virtual bool write(int32_t b) = 0;

        /**
         * Write a block of bytes.
         * Return true on success, false on failure.
         */
        virtual bool writeBytes(std::vector<uint8_t>& b, int32_t off, int32_t len) = 0;

        /**
         * Write a block of bytes to the end of the buffer.
         * buffer size.  Return true on success, false if
         * there is not enough room in the buffer.
         */
        //virtual bool writeBytes(const uint8_t* b, int32_t off, int32_t len) = 0;


        /**
         * Flush the stream.  Default implementation does nothing.
         */
        virtual void flush();

        /**
         * Close the stream.  Default implementation does nothing.
         */
        virtual void close();

        /**
         * Write a bool 1 or 0.
         * Return true on success or fail on failure.
         */
        bool writeBool(bool b);

        /**
         * Write a character.
         * Return true on success or fail on failure.
         */
        bool writeChar(char b);

        /**
         * Write the null terminated string.
         * Return true on success, false on failure.
         */
        bool writeStr(const std::string s);

        /**
         * Write a two byte integer in network byte order.
         * Return true on success, false on failure.
         */
        bool writeI2(int16_t v);

        /**
         * Write a four byte 
         * 32-bit
         *  integer in network byte order.
         * Return true on success, false on failure.
         */
        bool writeI4(int32_t v);

        /**
         * Write a eight byte 
         * 64-bit
         *  integer in network byte order.
         * Return true on success, false on failure.
         */
        bool writeI8(int64_t v);

        /**
         * Write a four byte 
         * 32-bit
         *  floating point value in network 
         * byte order.  Return true on success, false on failure.
         */
        bool writeF4(float v);

        /**
         * Write an eight byte 
         * 64-bit
         *  floating point value in network 
         * byte order.  Return true on success, false on failure.
         */
        bool writeF8(double v);

        /**
         * Print a newline character.  Return this.
         */
        OutStream* nl();

        /**
         * Print the specified string.  Return this.
         */
        OutStream* print(const std::string s);

        /**
         * Print the specified string left justified according
         * the specified padding width.  Return this.
         */
        OutStream* printPad(const std::string s, int32_t width);

        /**
         * Print a string for the specified boolean.  Return this.
         */
        OutStream* printBool(bool x);

        /**
         * Print the integer as a character.  Return this.
         */
        OutStream* printChar(int32_t x);

        /**
         * Print the integer as a signed decimal string.  Return this.
         */
        OutStream* printInt(int32_t x);

        /**
         * Print the integer as an unsigned hexadecimal string.
         */
        OutStream* printHex(int32_t x);

        /**
         * Print the long as a signed decimal string.  Return this.
         */
        OutStream* printLong(int64_t x);

        /**
         * Print the long as an unsigned hexadecimal string.
         */
        OutStream* printLongHex(int64_t x);

        /**
         * Print the float as a string.
         */
        OutStream* printFloat(float x);

        /**
         * Print the double as a string.
         */
        OutStream* printDouble(double x);

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
    }; // class OutStream

} // namespace sys

#endif
