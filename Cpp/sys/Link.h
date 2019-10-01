/*********************************************************
 * Header file for class 'Link'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Link
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_LINK_H
#define SYS_LINK_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "InStream.h"
#include "OutStream.h"


namespace sys {

// Forward declaration
class Link;

/**
 * Link models an execution relationship between a source
component's slot to a target component's slot.
 */
class Link {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        Link();



        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Propage the link
         */
        void propagate();

        /**
         * Save the link in binary format to the output stream.
         */
        void save(OutStream* out);

        /**
         * Load the link from a binary format input stream.
         * Return true on success, false on error.
         */
        bool load(InStream* in, int32_t fromComp);

        uint16_t fromComp = 0 /* DEF */;
        uint8_t fromSlot = 0 /* DEF */;
        Link* nextFrom;
        Link* nextTo;
        uint16_t toComp = 0 /* DEF */;
        uint8_t toSlot = 0 /* DEF */;
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
    }; // class Link

} // namespace sys

#endif
