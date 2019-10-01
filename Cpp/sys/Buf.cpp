/*********************************************************
 * Implementation for class 'Buf'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Buf
 * Generated: Mon Sep 23 21:20:17 CEST 2019
 *********************************************************/

 /* STL includes */
#include <stdint.h>
#include <fstream>
#include <iostream>
#include <iterator>
#include <sstream>
#include <vector>
#include <algorithm>
#include <assert.h>

#include "Buf.h"
#include "Sys.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

    /**
     * Construct a buffer with the specified maximum size.
     */
    Buf::Buf(int32_t maxBufLen) { allocBuf(maxBufLen); }

    /**
     * Constructor for 'Buf (_iInit_Obj)'
     */
    Buf::Buf() : Buf(32) {}

    /**
     * Constructor with byte array initializer
     */
    Buf::Buf(initializer_list<uint8_t> byteArray) {
        allocBuf(byteArray.size());
        memcpy(bytes, byteArray.begin(), bytesLen);
        size = bytesLen;
    }

    Buf::~Buf() { free(bytes); }

    /**
     * Get the byte at the specified index.
     */
    int32_t Buf::get(int32_t index) { return this->bytes[index]; }

    /**
     * Copy the specified buffer bytes into this buffer's bytes and
     * update size.  If the specified buffer contains more than
     * bytesLen bytes then the copy is truncated.
     */
    void Buf::copyFromBuf(const Buf& that) {
        copyFromBytes(that.bytes, 0, that.size);
        return;
    }

    /**
     * Copy the specified byte array into this buffer's bytes and
     * update size.  If the specified length is greater than
     * bytesLen bytes then the copy is truncated.
     */
    void Buf::copyFromBytes(const uint8_t* that, int32_t off, int32_t len) {
        int32_t newSize;

        newSize = len;
        if (len > this->bytesLen) {
            newSize = this->bytesLen;
        }
        this->size = static_cast<uint16_t>(newSize);
        Sys::copy(that, off, this->bytes, 0, newSize);
    }

    /**
     * Set size to zero.  The data remains untouched, but the
     * buffer is ready to be used to begin writing from scratch.
     */
    void Buf::clear() { this->size = static_cast<uint16_t>(0); }

    /**
     * Return the bytes cast to a Str reference.  No guarantee
     * is made whether the string is actually null terminated.
     */
    string Buf::toStr() {
        return static_cast<string>(reinterpret_cast<const char*>(this->bytes));
    }

    /**
     * Copy the specified string including its null terminator
     * into this buffer bytes field and update size accordingly.
     * If the specified string contains more than bytesLen characters
     * including the null terminator then return false and truncate
     * the copy.  If truncated we still add a null terminator.
     */
    bool Buf::copyFromStr(string s) {
        int32_t maxStringLen = min((size_t)this->bytesLen - 1, s.length());
        for (auto i = 0; i < maxStringLen; ++i) {
            this->bytes[i] = static_cast<uint8_t>(s[i]);
        }
        this->bytes[maxStringLen] = static_cast<uint8_t>(0);
        this->size = static_cast<uint16_t>(maxStringLen + 1);
        return bytesLen > s.length();
    }

    // region OPs

    /* Equal operators (required for tests */
    bool Buf::operator==(const Buf& buf) const {
        if (this->bytesLen == buf.bytesLen && this->size == buf.size) {
            return Sys::compareBytes(this->bytes, 0, buf.bytes, 0, size) == 0;
        } else {
            return 0;
        }
    }

    bool Buf::operator==(std::string s) const {
        if (size == s.length()) {
            auto i = 0;
            for (auto i = 0; i < size; i++) {
                if (bytes[i] != s[i]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /* Copy */
    Buf& Buf::operator=(Buf& buf) {
        if (bytesLen != buf.bytesLen) {
            free(bytes);
            bytes = (uint8_t*)malloc(buf.bytesLen);
        }
        memcpy(bytes, buf.bytes, buf.bytesLen);
        size = buf.size;
        bytesLen = buf.bytesLen;
        return *this;
    }

    Buf& Buf::operator=(std::string s) {
        copyFromStr(s);
        return *this;
    }

    void Buf::allocBuf(uint16_t size) {
        assert(size > 0);
        free(bytes);
        bytes = (uint8_t*)malloc(size);
        bytesLen = size;
        size = 0;
    }

}  // namespace sys
