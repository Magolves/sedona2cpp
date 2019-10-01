/*********************************************************
 * Implementation for class 'FileOutStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : FileOutStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FileOutStream.h"
#include "OutStream.h"
#include "Buf.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'FileOutStream 
 * _iInit
 * '
 */
FileOutStream::FileOutStream() {
	file.open("output.txt", std::fstream::out);
}


/**
 * Implementation of method 'write'
 */
bool FileOutStream::write(int32_t b) {
    if (fbuf.size >= (fbuf.bytesLen)) {
        if (!writeBufToFile()) {
            return false;
        }
    }
    fbuf.bytes[fbuf.size++] = static_cast<uint8_t>(b);
    return true;
}

/**
 * Implementation of method 'writeBytes'
 */
bool FileOutStream::writeBytes(vector<uint8_t>& b, int32_t off, int32_t len) {
    if (fbuf.size > 0) {
        writeBufToFile();
    }

	for (auto it = b.begin(); it != b.end(); ++it) {
		file << *it;
	}

	return true;
}



/**
 * Implementation of method 'flush'
 */
void FileOutStream::flush() {
    if (fbuf.size > 0) {
        writeBufToFile();
    }
    flush();
}


/**
 * Implementation of method 'close'
 */
void FileOutStream::close() {
    if (fbuf.size > 0) {
        writeBufToFile();
    }
}


/**
 * Implementation of method 'writeBufToFile'
 */
bool FileOutStream::writeBufToFile() {    
    file.write((char*)&fbuf.bytes[0], fbuf.size);

    fbuf.clear();
    return true;
}


/**
 * Destructor
 */
FileOutStream::~FileOutStream() {
	if (file.is_open()) {
		file.close();
	}
}

} // namespace sys
