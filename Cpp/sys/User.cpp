/*********************************************************
 * Implementation for class 'User'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : User
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "User.h"
#include "Component.h"
#include "Buf.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'User 
 * _iInit
 * '
 */
User::User() {
}


/**
 * Return the permissions available .
 */
int32_t User::permFor(const Component* c) {
    int32_t groups;
    int32_t perm;
    int32_t x;

    groups = (c->meta.get());
    perm = (this->perm.get());
    x = 0;
    if ((groups & 1) != 0) {
        x |= (perm & 255);
    }
    if ((groups & 2) != 0) {
        x |= ((perm >> 8) & 255);
    }
    if ((groups & 4) != 0) {
        x |= ((perm >> 16) & 255);
    }
    if ((groups & 8) != 0) {
        x |= ((perm >> 24) & 255);
    }
    return x;
}


/**
 * Return if this user has the specified permission 
 * on the given component.
 */
bool User::has(const Component* c, int32_t hasPerm) {
    if ((c->type) == "sys::User") {
        hasPerm = (User::ua);
    }
    return (permFor(c) & hasPerm) != 0;
}


/**
 * Can this user provisiong 
 * read/write
 *  the specified file.
 */
bool User::canProv(const string uri) {
    if (/* param */uri.rfind("app.", 0) == 0) {
        return ((this->prov) & (User::provApp)) != 0;
    }
	if (/* param */uri.rfind("kits.", 0) == 0) {
        return ((this->prov) & (User::provKits)) != 0;
    }
	if (/* param */uri.rfind("svm", 0) == 0) {
        return ((this->prov) & (User::provSvm)) != 0;
    }
    return true;
}


/**
 * Copy constructor for 'User'
 */
/**
 * Move constructor for 'User'
 */
/**
 * Destructor
 */
User::~User() {}

const int32_t User::admInvoke = 32;
const int32_t User::admRead = 8;
const int32_t User::admWrite = 16;
const int32_t User::opInvoke = 4;
const int32_t User::opRead = 1;
const int32_t User::opWrite = 2;
const int32_t User::provApp = 1;
const int32_t User::provKits = 2;
const int32_t User::provSvm = 4;
const int32_t User::ua = 64;
const string User::TYPE_NAME = "sys::User";
const string User::BASE_TYPE_NAME = "sys::Component";

} // namespace sys
