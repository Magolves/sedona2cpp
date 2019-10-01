/*********************************************************
 * Header file for class 'Slot'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Slot
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_SLOT_H
#define SYS_SLOT_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"


namespace sys {

// Forward declaration
class Slot;

/**
 * Slot models a property or action slot which is
available to the runtime via the reflection APIs.
 */
class Slot {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        Slot() = delete;

        Slot(std::string name) {
            this->name_ = name;
        }


        static const int32_t ACTION;
        static const int32_t AS_STR;
        static const int32_t CONFIG;
        static const int32_t OPERATOR;
        uint8_t flags = 0 /* DEF */;
        uint8_t id = 0 /* DEF */;

        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Is this a property slot.
         */
        bool isProp();

        /**
         * Is this an action slot.
         */
        bool isAction();

        /**
         * Is this a config property.
         */
        bool isConfig();

        /**
         * Is this a Buf property which should be treated as a Str.
         */
        bool isAsStr();

        /**
         * Is true if an operator level slot, or false if admin level.
         */
        bool isOperator();

        /**
         * Return if this slot is a property against the specified
         * filter:
         *    '*' = any property
         *    'c' = only config properties
         *    'r' = only runtime properties
         *    'C' = only operator level config properties
         *    'R' = only operator level runtime properties
         */
        bool matchProp(int32_t filter);

		/**
		 * Get the event mask for the property type.
		 */
		int watchEvent();


        std::string name_;
        std::string type;
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
    }; // class Slot

} // namespace sys

#endif
