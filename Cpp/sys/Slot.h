/*********************************************************
 * Header file for class 'Slot'.
 * (C) My Company 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Slot
 * Generated: Tue Oct 01 18:59:17 CEST 2019
 *********************************************************/

#ifndef SYS_SLOT_H
#define SYS_SLOT_H

// STL includes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>



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
         * Method '_iInit' (0)
         */
        Slot() = delete;

        Slot(std::string name) {
            this->name_ = name;
        }

        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Is this a property slot.
         */
        bool isProp() const;

        /**
         * Is this an action slot.
         */
        bool isAction() const;

        /**
         * Is this a config property.
         */
        bool isConfig() const;

        /**
         * Is this a Buf property which should be treated as a Str.
         */
        bool isAsStr() const;

        /**
         * Is true if an operator level slot, or false if admin level.
         */
        bool isOperator() const;

        /**
         * Return if this slot is a property against the specified
         * filter:
         *    '*' = any property
         *    'c' = only config properties
         *    'r' = only runtime properties
         *    'C' = only operator level config properties
         *    'R' = only operator level runtime properties
         */
        bool matchProp(int32_t filter) const;

        const uint8_t flags = 0;
        uint8_t id = 0;
        const std::string name = "";
        const std::string* type = nullptr;
        constexpr static const int32_t ACTION = 1;
        constexpr static const int32_t AS_STR = 4;
        constexpr static const int32_t CONFIG = 2;
        constexpr static const int32_t OPERATOR = 8;
        //endregion
    //endregion
    protected:

    //region Protected members/methods
        /**
         * Get the event mask for the property type.
         */
        int32_t watchEvent() const;


        //region Fields
        //endregion
    //endregion

    //region Private members/methods
    private:

        //region Fields
        uint16_t handle = 0;
        //endregion
    //endregion
    }; // class Slot

} // namespace sys

#endif
