/*********************************************************
 * Header file for class 'Service'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Service
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_SERVICE_H
#define SYS_SERVICE_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "Component.h"


namespace sys {

// Forward declaration
class Service;

/**
 * Service is a component which gets to execute background
work between execution cycles.  Services are also designed
to be looked up by type within an application.
 */
class Service : public Component {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        Service();

        /**
         * Virtual destructor
         */
        virtual ~Service();

        static const std::string TYPE_NAME;
        static const std::string BASE_TYPE_NAME;

        virtual const std::string typeName() const { return Service::TYPE_NAME; }
        virtual const std::string baseTypeName() const { return Service::BASE_TYPE_NAME; }


        //region Properties
        //endregion

        //region Public members/methods
        /**
         * Perform a chunk of background work.  Return true
         * 
         * if there is pending work or false if the service is
         * 
         * done working this cycle.
         * 
         * 
         * 
         * A service should be designed to function correctly no
         * 
         * matter how many times work is called per execution cycle.
         * 
         * Returning false is not a guarantee that work will not be
         * 
         * called again in a given execution cycle; rather, it is
         * 
         * a hint to the App execution engine that this service does
         * 
         * not have any more work to do.
         * 
         * 
         * 
         * If you only want to do work once per execution cycle, you should consider:
         * 
         * 
         * 
         * 1
         *  Moving your work into the execute
         * 
         *  callback. execute
         * 
         *  will only be
         * 
         * called once per execution cycle.
         * 
         * 
         * 
         * 2
         *  Set a "newCycle" flag in your execute
         * 
         *  method and unset it after
         * 
         * doing one work cycle.  Only do your work if the newCycle flag is set.
         */
        virtual bool work();

        /**
         * Return true if this Service will allow hibernation.
         * 
         * Default is to return true, subclass must override
         * 
         * if it has a need to prevent hibernation.
         */
        virtual bool canHibernate();

        /**
         * Callback when device is entering low-power sleep mode.
         */
        virtual void onHibernate();

        /**
         * Callback when device is exiting low-power sleep mode.
         */
        virtual void onUnhibernate();

        Service* nextService;
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
    }; // class Service

} // namespace sys

#endif
