/*********************************************************
 * Header file for class 'Kit'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Kit
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_KIT_H
#define SYS_KIT_H

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
class Kit;

/**
 * Kit is a top unit of modularity in a Sedona
environment and serves as the container for Types.
 */
class Kit {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        Kit();


        int32_t checksum = 0 /* DEF */;
        uint8_t id = 0 /* DEF */;
        uint8_t typesLen = 0 /* DEF */;

        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Get the type for the specified id or return null if out of range.
         */
        std::string type(int32_t id);

        /**
         * Find a type by its simple name unique to this
         * kit or return null if not found.
         */
        std::string findType(const std::string name);

        std::string name;
        std::vector<std::string> types;
        std::string version;
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
    }; // class Kit

} // namespace sys

#endif
