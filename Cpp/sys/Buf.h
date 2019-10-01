/*********************************************************
 * Header file for class 'Buf'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Buf
 * Generated: Mon Sep 23 21:20:17 CEST 2019
 *********************************************************/

#ifndef SYS_BUF_H
#define SYS_BUF_H

// STL includes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "Units.h"


namespace sys {

// Forward declaration
class Buf;

/**
 * Buf encapsulates a fixed capacity byte[].  The fixed
capacity is available via the 'bytesLen' field.  At any
time a variable number of bytes are actually used as
defined by the 'size' field.  Use the BufInStream and
BufOutStream classes to perform memory based IO against
a Buf instance.
 */
class Buf {

    //region Public members
    public:
        /**
         * Construct a buffer with the specified maximum size.
         */
        Buf(int32_t maxBufLen);

        /**
         * Method '_iInit_Obj' ([])
         */
        Buf();

		/**
		 * Constrcutor with byte array initializer
		 */
		Buf(std::initializer_list<uint8_t> byteArray);

        virtual ~Buf();


        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Get the byte at the specified index.
         */
        int32_t get(int32_t index);

        /**
         * Copy the specified buffer bytes into this buffer's bytes and 
         * update size.  If the specified buffer contains more than 
         * bytesLen bytes then the copy is truncated.
         */
        void copyFromBuf(const Buf& that);

        /**
         * Copy the specified byte array into this buffer's bytes and 
         * update size.  If the specified length is greater than 
         * bytesLen bytes then the copy is truncated.
         */
        void copyFromBytes(const uint8_t* that, int32_t off, int32_t len);

        /**
         * Set size to zero.  The data remains untouched, but the
         * buffer is ready to be used to begin writing from scratch.
         */
        void clear();

        /**
         * Return the bytes cast to a Str reference.  No guarantee
         * is made whether the string is actually null terminated.
         */
        std::string toStr();

        /**
         * Copy the specified string including its null terminator
         * into this buffer bytes field and update size accordingly.
         * If the specified string contains more than bytesLen characters
         * including the null terminator then return false and truncate
         * the copy.  If truncated we still add a null terminator.
         */
        bool copyFromStr(std::string s);

        // Operators
        bool operator==(const Buf& buf) const;
        bool operator==(const std::string s) const;
        Buf& operator=(Buf& buf);
        Buf& operator=(std::string s);

        uint8_t* bytes = {};
        uint16_t bytesLen = 0;
        uint16_t size = 0;
        //endregion
    //endregion
    private:
        void allocBuf(uint16_t size);
    }; // class Buf

} // namespace sys

#endif
