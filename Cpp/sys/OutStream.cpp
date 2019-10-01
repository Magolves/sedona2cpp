/*********************************************************
 * Implementation for class 'OutStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : OutStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "OutStream.h"
#include "Sys.h"
#include "OutStream.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'OutStream 
 * _iInit
 * '
 */
OutStream::OutStream() {
    ;
    /* Init virt InitVirt sys::OutStream */;
}


/**
 * Flush the stream.  Default implementation does nothing.
 */
void OutStream::flush() {
}


/**
 * Close the stream.  Default implementation does nothing.
 */
void OutStream::close() {
}


/**
 * Write a bool 1 or 0.
 * Return true on success or fail on failure.
 */
bool OutStream::writeBool(bool b) {
    //return write(((b == nullptr) ? 2 : (b ? 1 : 0)));
	return write(b ? 1 : 0);
}


/**
 * Write a character.
 * Return true on success or fail on failure.
 */
bool OutStream::writeChar(char b) {
    return write(b);
}


/**
 * Write the null terminated string.
 * Return true on success, false on failure.
 */
bool OutStream::writeStr(const string s) {
    int32_t i;

    for (i = 0; true; ++i)  {
        int32_t ch;

        ch = s[i];
        if (ch == 0) {
            break;
        }
        writeChar(ch);
    }
    return write(0);
}


/**
 * Write a two byte integer in network byte order.
 * Return true on success, false on failure.
 */
bool OutStream::writeI2(int16_t v) {
    //SwapEndian(v);
    return write(((v >> 8) & 255)) && write((v & 255));
}


/**
 * Write a four byte 
 * 32-bit
 *  integer in network byte order.
 * Return true on success, false on failure.
 */
bool OutStream::writeI4(int32_t v) {
    SwapEndian(v);
    return ((write(((v >> 24) & 255)) & write(((v >> 16) & 255))) & write(((v >> 8) & 255))) & write((v & 255));
}


/**
 * Write a eight byte 
 * 64-bit
 *  integer in network byte order.
 * Return true on success, false on failure.
 */
bool OutStream::writeI8(int64_t v) {
    //SwapEndian(v);
    return writeI4(static_cast<int32_t>(((v >> 32) & 4294967295))) & writeI4(static_cast<int32_t>((v & 4294967295)));
}


/**
 * Write a four byte 
 * 32-bit
 *  floating point value in network 
 * byte order.  Return true on success, false on failure.
 */
bool OutStream::writeF4(float v) {    
    int32_t f2b = Sys::floatToBits(v);
    //SwapEndian(f2b);
    return writeI4(f2b);
}


/**
 * Write an eight byte 
 * 64-bit
 *  floating point value in network 
 * byte order.  Return true on success, false on failure.
 */
bool OutStream::writeF8(double v) {
    //SwapEndian(v);
    return writeI8(Sys::doubleToBits(v));
}


/**
 * Print a newline character.  Return this.
 */
OutStream* OutStream::nl() {
    writeChar(10);
    flush();
    return this;
}


/**
 * Print the specified string.  Return this.
 */
OutStream* OutStream::print(const string s) {
    int32_t i;

    for (i = 0; true; ++i)  {
        int32_t ch;

        ch = s[i];
        if (ch == 0) {
            break;
        }
        writeChar(ch);
    }
    return this;
}


/**
 * Print the specified string left justified according
 * the specified padding width.  Return this.
 */
OutStream* OutStream::printPad(const string s, int32_t width) {
    int32_t i;

    i = 0;
    for (; true; ++i)  {
        int32_t ch;

		ch = s[i];
        if (ch == 0) {
            break;
        }
        writeChar(ch);
    }
    for (; i < width; ++i)  {
        writeChar(32);
    }
    return this;
}


/**
 * Print a string for the specified boolean.  Return this.
 */
OutStream* OutStream::printBool(bool x) {
    return print(x ? "true" : "false");
}


/**
 * Print the integer as a character.  Return this.
 */
OutStream* OutStream::printChar(int32_t x) {
    writeChar(x);
    return this;
}


/**
 * Print the integer as a signed decimal string.  Return this.
 */
OutStream* OutStream::printInt(int32_t x) {
    return print(Sys::intStr(x));
}


/**
 * Print the integer as an unsigned hexadecimal string.
 */
OutStream* OutStream::printHex(int32_t x) {
    return print(Sys::hexStr(x));
}


/**
 * Print the long as a signed decimal string.  Return this.
 */
OutStream* OutStream::printLong(int64_t x) {
    return print(Sys::longStr(x));
}


/**
 * Print the long as an unsigned hexadecimal string.
 */
OutStream* OutStream::printLongHex(int64_t x) {
    return print(Sys::longHexStr(x));
}


/**
 * Print the float as a string.
 */
OutStream* OutStream::printFloat(float x) {
    return print(Sys::floatStr(x));
}


/**
 * Print the double as a string.
 */
OutStream* OutStream::printDouble(double x) {
    return print(Sys::doubleStr(x));
}


/**
 * Copy constructor for 'OutStream'
 */
/**
 * Move constructor for 'OutStream'
 */
/**
 * Destructor
 */
OutStream::~OutStream() {}

} // namespace sys
