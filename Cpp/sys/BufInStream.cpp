/*********************************************************
 * Implementation for class 'BufInStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : BufInStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "BufInStream.h"
#include "InStream.h"
#include "Sys.h"
#include "Buf.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Construct for specified Buf instance.
 */
BufInStream::BufInStream(Buf* buf) {
    ;
    /* Init virt InitVirt sys::BufInStream */;
    (this->buf) = buf;
}


/**
 * Constructor for 'BufInStream 
 * _iInit_InStream
 * '
 */
BufInStream::BufInStream() {
}


/**
 * Read the next byte at pos and increment pos.
 * Return -1 if pos >= size.
 */
int32_t BufInStream::read() {
    Buf* buf;

    buf = (this->buf);
    if ((this->pos) >= (buf->size)) {
        return -1;
    }
    return (buf->bytes)[(this->pos)++];
}


/**
 * Read the next block of available bytes starting from pos.
 * Return the number of bytes copied into b and advance
 * the position likewise.
 */
int32_t BufInStream::readBytes(vector<uint8_t>& b, int32_t off, int32_t len) {
    Buf* buf;

    buf = (this->buf);
    if (((buf->size) - (this->pos)) < len) {
        len = ((buf->size) - (this->pos));
    }
    Sys::copy((buf->bytes), (this->pos), &b[0], off, len);
    (this->pos) += static_cast<uint16_t>(len);
    return len;
}

/**
 * Read the next block of available bytes starting from pos.
 * Return the number of bytes copied into b and advance
 * the position likewise.
 */
int32_t BufInStream::readBytes(uint8_t* b, int32_t off, int32_t len) {
    Buf* buf;

    buf = (this->buf);
    if (((buf->size) - (this->pos)) < len) {
        len = ((buf->size) - (this->pos));
    }
    Sys::copy((buf->bytes), (this->pos), &b[0], off, len);
    (this->pos) += static_cast<uint16_t>(len);
    return len;
}


/**
 * Read the current position as a null terminated
 * string.  Return null on error.
 */
string BufInStream::readStrInPlace(const string str) {
    Buf* buf;
    int32_t off;

    buf = (this->buf);
    off = (this->pos);
    while ((buf->bytes)[(this->pos)++] != 0)  {
        if ((this->pos) >= (buf->size)) {
            return nullptr;
        }
    }

	buf = (this->buf);
	off = (this->pos);

	string s = "";
	int i = 0;
	while ((buf->bytes)[(this->pos)++] != 0) {
		s[i++] = buf->bytes[this->pos];
	}
    return s;
}


/**
 * Rewind sets the pos back to zero to begin
 * reading the Buf from the beginning.
 */
void BufInStream::rewind() {
    (this->pos) = static_cast<uint16_t>(0);
}


/**
 * Copy constructor for 'BufInStream'
 */
/**
 * Move constructor for 'BufInStream'
 */
/**
 * Destructor
 */
BufInStream::~BufInStream() {}

} // namespace sys
