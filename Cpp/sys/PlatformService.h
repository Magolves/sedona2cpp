/*********************************************************
 * Header file for class 'PlatformService'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : PlatformService
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_PLATFORMSERVICE_H
#define SYS_PLATFORMSERVICE_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "Buf.h"
#include "Service.h"


namespace sys {

// Forward declaration
class PlatformService;

/**
 * PlatformService is a service designed for a
specific hardware/OS platform.
 */
class PlatformService : public Service {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        PlatformService();

        /**
         * Virtual destructor
         */
        virtual ~PlatformService();

        static const std::string TYPE_NAME;
        static const std::string BASE_TYPE_NAME;

        virtual const std::string typeName() const { return PlatformService::TYPE_NAME; }
        virtual const std::string baseTypeName() const { return PlatformService::BASE_TYPE_NAME; }

        static const int32_t netConfigError;
        static const int32_t netFatal;
        static const int32_t netInitializing;
        static const int32_t netOk;

        //region Properties
        /**
         * Property 'memAvailable'
         */
        Property<int64_t> memAvailable = Property<int64_t>("memAvailable", 0l);

        /**
         * Property 'platformId'
         */
        Property<std::string> platformId = Property<std::string>("platformId", "unknown");

        /**
         * Property 'platformVer'
         */
        Property<std::string> platformVer = Property<std::string>("platformVer", "0");

        //endregion

        //region Public members/methods
        /**
         * Lookup the platform service for this VM.  Return null
         * if the application hasn't booted yet or is missing
         * a platform service.
         */
        static PlatformService* lookup();

        /**
         * Initialize the platform with the command line arguments.
         * Return 0 on success.  Return  non-zero on failure, which will
         * terminate the process.
         */
        virtual int32_t init(const std::vector<std::string>& args, int32_t argsLen);

        /**
         * Notify the platform of a name/value pair setting
         * or state change.
         */
        virtual void notify(const std::string key, const std::string val);

        /**
         * Convenience to set the platformId property, and call changed
         */
        void updatePlatformId(const std::string id);

        /**
         * Convenience to set the platformVer property, and call changed
         */
        void updatePlatformVer(const std::string ver);

        /**
         * Method 'start' 
         * [OVERRIDE, PUBLIC, VIRTUAL]
         */
        virtual void start() override ;

        /**
         * Method 'execute' 
         * [OVERRIDE, PUBLIC, VIRTUAL]
         */
        virtual void execute() override ;

        /**
         * By default, a Platform Service assumes that the platform does not
         * support hibernation, so this method always returns false.  If your
         * platform supports hibernation, you should override this method to
         * return true.
         */
        virtual bool canHibernate() override ;

        /**
         * Update the memAvailable property to reflect the current
         * amount of available memory.
         */
        virtual void refreshMemory();

        /**
         * Return true if App must yield CPU by exiting VM each cycle.
         * 
         * Platforms that support pre-emptive multitasking should return false
         * since App can sleep to yield CPU.
         * 
         * Cooperative multitasking platforms that expect tasks to run to
         * completion and return so other tasks can execute should return true.
         * 
         * See also the Apps chapter in Sedona docs
         */
        virtual bool yieldRequired();

        /**
         * Inform platform that SVM will be exiting with Err.yield and needs
         * to be scheduled to run again 
         * via resume in vm.c
         *  in ns nanoseconds to meet
         * the desired App.scanPeriod value.
         * 
         * If the application overruns its scan period, yieldTime will be negative.
         * In this case the VM should be scheduled to run again as soon as possible.
         * 
         * See also the Apps chapter in Sedona docs
         */
        virtual void yield(int64_t yieldTime);

        /**
         * Returns true if the platform wants to the App to invoke work
         * 
         *  again on
         * all Services during free time before the next App execute needs to begin.
         * 
         * For Apps with long scanPeriods 
         * greater than 50ms
         * , this allows Services 
         * to respond to network traffic or other asynchronous events multiple times 
         * during a single scanPeriod - even if the service didn't have any immediate 
         * work to do.  
         * 
         * i.e. - Sox services responds to a poll request, but then has no further
         * work, nor doing any other services.  A few ms later, another poll request 
         * comes in.  If this method returns false, no further workd will be done 
         * until the beginning of the next scan period  If this method returns true, 
         * work
         * 
         *  will be called on all services again and the poll request will
         * be handled before the next scanPeriod.  
         * 
         * nsUntilDeadline represents the time remaining before the next App 
         * execute
         * 
         *  cycle again.  Depending on the underlying OS/system 
         * architecture, the platform may want to sleep or otherwise relinquish the 
         * CPU for some fraction of nsUntilDeadline before returning and
         * allowing App to call service work again.
         * 
         * See also the Apps chapter in Sedona docs
         */
        virtual bool workDuringFreeTime(int64_t nsUntilDeadline);

        /**
         * Return the current state of the network interface.  For devices
         * that support multiple interfaces, return the state of the primary
         * network interface on the platform.
         */
        virtual int32_t getNetworkState();

        /**
         * Action to restart the Sedona application on the host platform.
         * This typically means restart just the Sedona process.
         */
        virtual void restart();

        /**
         * Action to reboot the entire host platform.  This typically
         * means to reboot the entire operating system.
         */
        virtual void reboot();

        /**
         * Get the platform identifier which defines how
         * this Sedona device should be provisioned.
         */
        static std::string doPlatformId();

        /**
         * Get the platform SVM version.
         */
        static std::string getPlatVersion();

        /**
         * Returns number of bytes available on the native platform.
         */
        static int64_t getNativeMemAvailable();

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
    }; // class PlatformService

} // namespace sys

#endif
