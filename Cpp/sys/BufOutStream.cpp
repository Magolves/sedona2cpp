/*********************************************************
 * Implementation for class 'BufOutStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : BufOutStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>
#include <assert.h>

#include "BufOutStream.h"
#include "Sys.h"
#include "OutStream.h"
#include "Buf.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Construct for specified Buf instance.
 */
BufOutStream::BufOutStream(Buf* buf) { 
    assert(buf != nullptr);
    this->buf = buf;
}

/**
 * Write a byte to the end of the buffer at buf[size].
 * Return true on success, false if buffer is full.
 */
bool BufOutStream::write(int32_t x) {
	if (buf->size >= buf->bytesLen) {
		return false;
	}
    
    this->buf->bytes[this->buf->size++] = x;	
	return true;
}

/**
 * Write a block of bytes to the end of the buffer.
 * buffer size.  Return true on success, false if
 * there is not enough room in the buffer.
 */
bool BufOutStream::writeBytes(vector<uint8_t>& b, int32_t off, int32_t len) {
    return writeBytes(&b[0], off, len);
}

/**
 * Write a block of bytes to the end of the buffer.
 * buffer size.  Return true on success, false if
 * there is not enough room in the buffer.
 */
bool BufOutStream::writeBytes(const uint8_t* b, int32_t off, int32_t len) {
    if (((buf->size) + len) >= (buf->bytesLen)) {
        return false;
    }

    Sys::copy(b, off, buf->bytes, buf->size, len);
    buf->size += len;
    return true;
}

/**
 * Destructor
 */
BufOutStream::~BufOutStream() {}

} // namespace sys
