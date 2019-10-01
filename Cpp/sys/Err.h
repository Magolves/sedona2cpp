/*********************************************************
 * Header file for class 'Err'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Err
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_ERR_H
#define SYS_ERR_H

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
class Err;

/**
 * Errors codes are assigned as follows:

  Non-recoverable:
    [C code: 1-39]
    [Sedona: 40-99]
    - don't attempt to auto-restart application because 
      the vm, scode, or app itself is invalid

  Recoverable: 
    [C code: 100-139]
    [Sedona: 140-249]
    - something went wrong at runtime, but if we auto-restart 
      the app things will probably start working again (at 
      least for a little while)

  Special: 
    [250-255]
    - special codes shared b/w C and Sedona code
 */
class Err {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        Err();


        static const int32_t badPlatformService;
        static const int32_t cannotInitApp;
        static const int32_t cannotInsert;
        static const int32_t cannotLoadLink;
        static const int32_t cannotMalloc;
        static const int32_t cannotOpenFile;
        static const int32_t hibernate;
        static const int32_t invalidAppEndMarker;
        static const int32_t invalidArgs;
        static const int32_t invalidCompEndMarker;
        static const int32_t invalidKitId;
        static const int32_t invalidMagic;
        static const int32_t invalidSchema;
        static const int32_t invalidTypeId;
        static const int32_t invalidVersion;
        static const int32_t nameTooLong;
        static const int32_t noPlatformService;
        static const int32_t restart;
        static const int32_t unexpectedEOF;
        static const int32_t yield;

        //region Properties
        //endregion

        //region Public members/methods
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
    }; // class Err

} // namespace sys

#endif
