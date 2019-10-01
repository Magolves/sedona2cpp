/*********************************************************
 * Implementation for class 'InStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : InStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "InStream.h"
#include "Sys.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'InStream 
 * _iInit
 * '
 */
InStream::InStream() {
    ;
    /* Init virt InitVirt sys::InStream */;
}


/**
 * Close the stream.  Default implementation does nothing.
 */
void InStream::close() {
}


/**
 * Read a boolean value of zero or non-zero.
 */
bool InStream::readBool() {
    int32_t x;

    x = read();
	/* OUR BOOL CANNOT BE NULL
    if (x == 2) {
        return nullptr;
    }*/
    return x != 0;
}


/**
 * Read a single character 
 * right now we only read ASCII
 * .
 */
int32_t InStream::readChar() {
    return read();
}


/**
 * Read a null terminated string into a buffer.
 * Return true on success, false on error or end
 * of stream.
 */
bool InStream::readStr(string& s, int32_t max) {
    int32_t i = 1;

    // clear string first
    s = "";
    while (i < max)  {
        char ch = readChar();
        if (ch < 0) {
            return false;
        }        
        if (ch == 0) {
            return true;
        }
        s += ch;
        ++i;
    }
    // read imaginary zero byte
    read();
    return false;
}


/**
 * Read an unsigned two byte integer in network
 * byte order.  Return -1 if end of stream.
 */
int32_t InStream::readU2() {
    int32_t a;
    int32_t b;

    a = read();
    b = read();
    if ((a < 0)||(b < 0)) {
        return -1;
    }
    return (a << 8) | b;
}


/**
 * Read an signed four byte 
 * 32-bit
 *  integer in network
 * byte order.  Return -1 if end of stream.
 */
int32_t InStream::readS4() {
    int32_t a;
    int32_t b;
    int32_t c;
    int32_t d;

    a = read();
    b = read();
    c = read();
    d = read();
    if ((a < 0)||(b < 0)||(c < 0)||(d < 0)) {
        return -1;
    }

    return (((a << 24) | (b << 16)) | (c << 8)) | d;
}


/**
 * Read an signed eight byte 
 * 64-bit
 *  integer in network
 * byte order.  Return -1 if end of stream.
 */
int64_t InStream::readS8() {
    return (static_cast<int64_t>(readS4()) << 32) | (static_cast<int64_t>(readS4()) & 4294967295);
}


/**
 * Read a four byte 
 * 32-bit
 *  floating point value in network byte order
 */
float InStream::readF4() {
    return Sys::bitsToFloat(readS4());
}


/**
 * Read a eight byte 
 * 64-bit
 *  floating point value in network byte order
 */
double InStream::readF8() {
    return Sys::bitsToDouble(readS8());
}


/**
 * Skip n number of bytes.
 */
void InStream::skip(int32_t n) {
    while (n-- > 0)  {
        read();
    }
}


/**
 * Copy constructor for 'InStream'
 */
/**
 * Move constructor for 'InStream'
 */
/**
 * Destructor
 */
InStream::~InStream() {}

} // namespace sys
