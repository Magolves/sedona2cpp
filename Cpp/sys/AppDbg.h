/*********************************************************
 * Header file for class 'AppDbg'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : AppDbg
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_APPDBG_H
#define SYS_APPDBG_H

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
#include "Component.h"


namespace sys {

// Forward declaration
class AppDbg;

/**
 * AppDbg is an optional component which displays performance data on the scan engine
This object should only be used during tuning of an application
and should not be a permanent part.
Note that inclusion of this component increases the execute
cycle time and memory footprint, so it is not "non-invasive"
 */
class AppDbg : public Component {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        AppDbg();

        /**
         * Virtual destructor
         */
        virtual ~AppDbg();

        static const std::string TYPE_NAME;
        static const std::string BASE_TYPE_NAME;

        virtual const std::string typeName() const { return AppDbg::TYPE_NAME; }
        virtual const std::string baseTypeName() const { return AppDbg::BASE_TYPE_NAME; }


        //region Properties
        /**
         * Property 'enable'
         */
        Property<bool> enable = Property<bool>("enable");

        /**
         * Property 'execAvg'
         */
        Property<float> execAvg = Property<float>("execAvg");

        /**
         * Property 'execMax'
         */
        Property<float> execMax = Property<float>("execMax");

        /**
         * Property 'execMaxMax'
         */
        Property<float> execMaxMax = Property<float>("execMaxMax");

        /**
         * Property 'execMin'
         */
        Property<float> execMin = Property<float>("execMin");

        /**
         * Property 'execMinMin'
         */
        Property<float> execMinMin = Property<float>("execMinMin");

        /**
         * Property 'overruns'
         */
        Property<int32_t> overruns = Property<int32_t>("overruns", 0);

        /**
         * Property 'print'
         */
        Property<bool> print = Property<bool>("print");

        /**
         * Property 'scanAvg'
         */
        Property<float> scanAvg = Property<float>("scanAvg");

        /**
         * Property 'scanCyclesPerUpdate'
         */
        Property<int32_t> scanCyclesPerUpdate = Property<int32_t>("scanCyclesPerUpdate", 1000);

        /**
         * Property 'scanMax'
         */
        Property<float> scanMax = Property<float>("scanMax");

        /**
         * Property 'scanMaxMax'
         */
        Property<float> scanMaxMax = Property<float>("scanMaxMax");

        /**
         * Property 'scanMin'
         */
        Property<float> scanMin = Property<float>("scanMin");

        /**
         * Property 'scanMinMin'
         */
        Property<float> scanMinMin = Property<float>("scanMinMin");

        /**
         * Property 'workAvg'
         */
        Property<float> workAvg = Property<float>("workAvg");

        /**
         * Property 'workMax'
         */
        Property<float> workMax = Property<float>("workMax");

        /**
         * Property 'workMaxMax'
         */
        Property<float> workMaxMax = Property<float>("workMaxMax");

        /**
         * Property 'workMin'
         */
        Property<float> workMin = Property<float>("workMin");

        /**
         * Property 'workMinMin'
         */
        Property<float> workMinMin = Property<float>("workMinMin");

        //endregion

        //region Public members/methods
        /**
         * Method 'start' 
         * [OVERRIDE, PUBLIC, VIRTUAL]
         */
        virtual void start() override ;

        /**
         * Detect if enable has changed from false to true, if so
         * reset all the calculation variables
         */
        virtual void changed(const Slot* slot) override ;

        /**
         * Calculate and cache previous scan's times every scan
         */
        virtual void execute() override ;

        /**
         * Reset the max value calculations and the overruns value
         */
        virtual void resetValues();

        bool first = false /* DEF */;
        float execTime = 0.0f /* DEF */;
        float execTimeRunningTotal = 0.0f /* DEF */;
        float scanTime = 0.0f /* DEF */;
        float scanTimeRunningTotal = 0.0f /* DEF */;
        float tmpExecTime = 0.0f /* DEF */;
        float tmpExecTimeMax = 0.0f /* DEF */;
        float tmpExecTimeMin = 0.0f /* DEF */;
        float tmpScanTime = 0.0f /* DEF */;
        float tmpScanTimeMax = 0.0f /* DEF */;
        float tmpScanTimeMin = 0.0f /* DEF */;
        float tmpWorkTime = 0.0f /* DEF */;
        float tmpWorkTimeMax = 0.0f /* DEF */;
        float tmpWorkTimeMin = 0.0f /* DEF */;
        float workTime = 0.0f /* DEF */;
        float workTimeRunningTotal = 0.0f /* DEF */;
        //endregion
    //endregion
    protected:

    //region Protected members/methods

        //region Fields
        float execTime = 0.0f /* DEF */;
        float execTimeRunningTotal = 0.0f /* DEF */;
        float scanTime = 0.0f /* DEF */;
        float scanTimeRunningTotal = 0.0f /* DEF */;
        float tmpExecTime = 0.0f /* DEF */;
        float tmpExecTimeMax = 0.0f /* DEF */;
        float tmpExecTimeMin = 0.0f /* DEF */;
        float tmpScanTime = 0.0f /* DEF */;
        float tmpScanTimeMax = 0.0f /* DEF */;
        float tmpScanTimeMin = 0.0f /* DEF */;
        float tmpWorkTime = 0.0f /* DEF */;
        float tmpWorkTimeMax = 0.0f /* DEF */;
        float tmpWorkTimeMin = 0.0f /* DEF */;
        float workTime = 0.0f /* DEF */;
        float workTimeRunningTotal = 0.0f /* DEF */;
        //endregion
    //endregion

    //region Private members/methods
    private:

        //region Fields
        //endregion
    //endregion
    }; // class AppDbg

} // namespace sys

#endif
