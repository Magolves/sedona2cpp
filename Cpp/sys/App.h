/*********************************************************
 * Header file for class 'App'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : App
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_APP_H
#define SYS_APP_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "Link.h"
#include "Slot.h"
#include "Component.h"
#include "InStream.h"
#include "OutStream.h"
#include "Log.h"
#include "Watch.h"
#include "Buf.h"
#include "PlatformService.h"
#include "Service.h"


namespace sys {

// Forward declaration
class App;

/**
 * App encapsulates an application database which includes
the list of available Types, the Component instances,
their configuration, and the Links.
 */
class App : public Component {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        App();

        /**
         * Virtual destructor
         */
        virtual ~App();

        static const std::string TYPE_NAME;
        static const std::string BASE_TYPE_NAME;

        virtual const std::string typeName() const { return App::TYPE_NAME; }
        virtual const std::string baseTypeName() const { return App::BASE_TYPE_NAME; }


        //region Properties
        /**
         * Property 'appName'
         */
        Property<std::string> appName = Property<std::string>("appName");

        /**
         * Property 'deviceName'
         */
        Property<std::string> deviceName = Property<std::string>("deviceName");

        /**
         * Property 'guardTime'
         */
        Property<int32_t> guardTime = Property<int32_t>("guardTime", 5);

        /**
         * Property 'hibernationResetsSteadyState'
         */
        Property<bool> hibernationResetsSteadyState = Property<bool>("hibernationResetsSteadyState", false);

        /**
         * Property 'scanPeriod'
         */
        Property<int32_t> scanPeriod = Property<int32_t>("scanPeriod", 50);

        /**
         * Property 'timeToSteadyState'
         */
        Property<int32_t> timeToSteadyState = Property<int32_t>("timeToSteadyState", 0);

        //endregion

        //region Public members/methods
        /**
         * Prepare the applications data structures to begin
         * configuration.
         */
        bool initApp(int32_t initCompsLen);

        /**
         * Free all the dynamic memory associated with
         * this application.
         */
        void cleanupApp();

        /**
         * Is the application currently running
         */
        bool isRunning();

        /**
         * Has the application reached steady state 
         * as defined by timeToSteadyState
         */
        bool isSteadyState();

        /**
         * Start all the components.  Return 0 on success,
         * error code on failure.
         */
        int32_t startApp(const std::vector<std::string>& args, int32_t argsLen);

        /**
         * Stop all the components.
         */
        void stopApp();

        /**
         * Run this application - right now this is a
         * simple round robin execution.
         */
        int32_t runApp();

        /**
         * Run this application - right now this is a
         * simple round robin execution.
         */
        int32_t resumeApp();

        /**
         * Lookup a component by id or null.
         */
        Component* lookup(int32_t id);

        /**
         * Get the maximum component id used by the application.
         */
        int32_t maxId();

        /**
         * Lookup a service by type 
         * or base type
         * .  Return
         * null if there are no registered services which
         * implement the specified type.
         */
        Service* lookupService(const std::string type);

        /**
         * Add a component to the application.
         * Return the new Component on success, null on failure.
		 * NOTE: Does NOT match the Sedona method!
         */
		Component& add(Component& parent, Component& newComponent);

        /**
         * Remove the specified component and free it's memory.
         * This method automatically recursively removes any children
         * of the component first.  Return true on success, false on
         * failure.
         */
        bool remove(Component& c);

        /**
         * Gets first child component of a given type.
         * Returns null if no objects found, otherwise returns
         * a Component
         */
        Component* getFirstChildOfType(const Component* parent, const std::string t);

        /**
         * Gets next sibling of a component that is of type t.
         * Returns null if end of sibling list is reached without finding one.
         */
        Component* getNextSiblingOfType(const Component* component, const std::string t);

        /**
         * Find a link with the specified from and to
         * ids or return null if not found.
         */
        Link* lookupLink(int32_t fromCompId, int32_t fromSlotId, int32_t toCompId, int32_t toSlotId);

        /**
         * Add a new Link into the application by registering it in both
         * the "to" and "from" component's linked-list of Links.  Return
         * the new Link or null on error.
         */
        Link* addLink(Component* from, const Slot* fromSlot, Component* to, const Slot* toSlot);

        /**
         * Remove a Link from the application by unregistering it from both
         * the "to" and "from" component's list linked of Links.  Return
         * true on success, false on failure.
         */
        bool removeLink(const Link* link);

        /**
         * Each service which uses watches, should call this
         * method on startup with it's service specific array
         * of Watch subclasses.
         */
        void initWatches(const std::vector<Watch>& subclasses);

        /**
         * Allocate a watch for a service using watches.  The service
         * should pass in its service specific array of Watch subclasses.
         * If a watch is opened then it is reserved, its closed field is
         * set to false, and the subclass instance is returned.  If all
         * the watches are currently open, then return null.
         */
        Watch* openWatch(const std::vector<Watch>& subclasses);

        /**
         * Close the specific watch by freeing its id to be used
         * again and setting its closed field to false.
         */
        void closeWatch(Watch* watch);

        /**
         * Save the app in binary format to the output stream:
         * 
         *   app
         *   {
         *     u4           magic 0x73617070 "sapp"
         *     u4           version 0x0002 0.2
         *     Schema       schema
         *     Component[]  comps
         *     u2           0xffff end of comps marker
         *     Link[]       links
         *     u2           0xffff end of links marker
         *     u1           '.' end of app marker
         *   }
         * 
         * Return 0 on success, non-zero on failure.
         */
        int32_t saveApp(OutStream* out);

        /**
         * Load the app from a binary format input stream.
         * Return 0 on success or non-zero on error.
         */
        int32_t loadApp(InStream* in);

        /**
         * Schema is the list of kit names and checksums:
         * 
         *   schema
         *   {
         *     u1  count
         *     kits[count]
         *     {
         *       str  name
         *       u4   checksum
         *     }
         *   }
         */
        void saveSchema(OutStream* out);

        /**
         * Check that the schema on the input stream matches
         * the schema of the current runtime.
         * 
         * In this version, we allow the app to depend on fewer kits than 
         * the scode, as long as all the app's kits are in the scode.
         */
        bool loadSchema(InStream* in);

        /**
         * Load the application from persistent storage
         */
        int32_t load();

        /**
         * Save the application back to persistent storage
         */
        virtual void save();

        /**
         * Action to request hibernation.  Current execute loop will
         * complete and all services will get a chance to work before
         * hibernation occurs
         */
        virtual void hibernate();

        /**
         * Save the application and then exit the main loop.
         */
        virtual void quit();

        /**
         * Action to invoke Platform.restart.
         */
        virtual void restart();

        /**
         * Action to invoke Platform.reboot.
         */
        virtual void reboot();

        std::vector<Component> comps;
        int32_t compsLen = 0 /* DEF */;
        int64_t cycleCount = 0L /* DEF */;
        std::vector<int32_t> kitIdMap;
        int64_t lastEndWork = 0l;
        int64_t lastStartExec = 0l;
        int64_t lastStartWork = 0l;
        int64_t newStartExec = 0l;
        PlatformService* platform;
        Service* services;
        std::vector<Watch> watches;
        static Log log;
        //endregion
    //endregion
    protected:

    //region Protected members/methods

        //region Fields
        bool atSteadyState = false;
        std::fstream* file;
        uint8_t runStatus = 0;
        bool running = false /* DEF */;
        int64_t steadyAt = 0l;
        //endregion
    //endregion

    //region Private members/methods
    private:
        /**
         * Recursively execute the component and its children.
         */
        void executeTree(Component& c);

        /**
         * Allocate the next component id.  If no more space
         * is left in the comps array, then automatically
         * grow it.  Return -1 on error.
         */
        int32_t allocCompId();

        /**
         * Grow the comps array if too small.  Return true on success.
         */
        bool ensureCompsCapacity(int32_t newLen);

        /**
         * Private implementation of adding a component into
         * the lookup tables - this method does NOT manage
         * the parent/child relationship.  Return true on
         * success, false on failure.
         */
        bool insert(int32_t id, const Component* c);

        /**
         * Insert a Link into the application by registering it in both
         * the "to" and "from" component's list linked of Links.  Return
         * true on success, false on failure.
         */
        bool insertLink(Link* link);


        //region Fields
        //endregion
    //endregion
    }; // class App

} // namespace sys

#endif
