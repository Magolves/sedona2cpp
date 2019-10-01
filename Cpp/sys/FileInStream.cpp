/*********************************************************
 * Implementation for class 'FileInStream'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : FileInStream
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FileInStream.h"
#include "InStream.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'FileInStream 
 * _iInit
 * '
 */
FileInStream::FileInStream() {
    ;
    /* Init virt InitVirt sys::FileInStream */;
}


/**
 * Implementation of method 'read'
 */
int32_t FileInStream::read() {
	//return file->read();
	return -1;
}


/**
 * Implementation of method 'readBytes'
 */
int32_t FileInStream::readBytes(vector<uint8_t>& b, int32_t off, int32_t len) {
    //return readBytes((this->file), b, off, len);
	return -1;
}


/**
 * Implementation of method 'close'
 */
void FileInStream::close() {
}


/**
 * Copy constructor for 'FileInStream'
 */
/**
 * Move constructor for 'FileInStream'
 */
/**
 * Destructor
 */
FileInStream::~FileInStream() {}

} // namespace sys
