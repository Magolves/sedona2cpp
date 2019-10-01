/*********************************************************
 * Header file for class 'Watch'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Watch
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_WATCH_H
#define SYS_WATCH_H

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
class Watch;

/**
 * Watch is used to manage an event queue of component changes.
Every component is allocated a byte per potential watch determined
by the Watch.max define.  To use the watch framework:
  - Create your own subclass of Watch
  - In your service create an array of your Watch subclass
  - Call App.initWatches() on service startup with your subclass array
  - To allocate a watch call App.openWatch() passing in the
    array of your subclasses
  - When you close a watch, make sure you call App.closeWatch()
 */
class Watch {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        Watch();

        /**
         * Virtual destructor
         */
        virtual ~Watch();

        static const int32_t eventAll;
        static const int32_t eventConfig;
        static const int32_t eventLinks;
        static const int32_t eventRuntime;
        static const int32_t eventTree;
        static const int32_t max;
        static const int32_t subAll;
        static const int32_t subConfig;
        static const int32_t subLinks;
        static const int32_t subRuntime;
        static const int32_t subTree;

        //region Properties
        //endregion

        //region Public members/methods
        /**
         * The watch id is a combination of the count in the
         * high byte and the index in the low byte.
         */
        int32_t id();

        /**
         * Map a subscription bit to its ASCII code.
         */
        static int32_t fromSubBit(int32_t subBit);

        /**
         * Map ASCII codes to subscription bit:
         *  't' = subTree
         *  'c' = subConfig
         *  'r' = subRuntime
         *  'l' = subLink
         *  '*' = subAll
         */
        static int32_t toSubBit(int32_t what);

        /**
         * Map ASCII codes to event bit:
         *  't' = eventTree
         *  'c' = eventConfig
         *  'r' = eventRuntime
         *  'l' = eventLink
         *  '*' = eventAll
         */
        static int32_t toEventBit(int32_t what);

        bool closed = false /* DEF */;
        uint8_t index = 0 /* DEF */;
        uint8_t rand = 0 /* DEF */;
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
    }; // class Watch

} // namespace sys

#endif
