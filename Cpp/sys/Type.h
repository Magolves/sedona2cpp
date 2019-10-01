/*********************************************************
 * Header file for class 'Type'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Type
 * Generated: Tue Aug 27 17:07:47 CEST 2019
 *********************************************************/

#ifndef SYS_TYPE_H
#define SYS_TYPE_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "Slot.h"
#include "Kit.h"


namespace sys {

// Forward declaration
class Type;

class Type {

    //region Public members
    public:
        /**
         * Method '_iInit'
         */
        Type();


        static const int32_t voidId;
        static const int32_t boolId;
        static const int32_t byteId;
        static const int32_t shortId;
        static const int32_t intId;
        static const int32_t longId;
        static const int32_t floatId;
        static const int32_t doubleId;
        static const int32_t maxPrimitiveId;
        static const int32_t bufId;
        static const int32_t errorId;
        uint8_t id = 0 /* DEF */;
        uint16_t sizeOf = 0 /* DEF */;
        uint16_t instanceInitMethod = 0 /* DEF */;
        uint8_t slotsLen = 0 /* DEF */;

        //region Getters and setters
        //endregion

        //region Public members/methods
        /**
         * Method 'is'
         */
        bool is(const Type* t);

        /**
         * Method 'isPrimitive'
         */
        bool isPrimitive();

        /**
         * Method 'slot'
         */
        Slot* slot(int32_t id);

        /**
         * Method 'findSlot'
         */
        Slot* findSlot(const std::string name);

        //endregion
    //endregion
    protected:

    //region Protected members/methods

        //region Fields
        std::string name;
        Kit* kit;
        Type* base;
        std::vector<Slot> slots;
        //endregion
    //endregion

    //region Private members/methods
    private:

        //region Fields
        //endregion
    //endregion
    }; // class Type

} // namespace sys

#endif

