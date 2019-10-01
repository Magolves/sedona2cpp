/*********************************************************
 * Header file for class 'Component'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Component
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_COMPONENT_H
#define SYS_COMPONENT_H

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
#include "Buf.h"


namespace sys {

// Forward declaration
class Component;

/**
 * Component is the base class for all Sedona component classes.
 */
class Component {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        Component();

        /**
         * Virtual destructor
         */
        virtual ~Component();

        static const std::string TYPE_NAME;
        static const std::string BASE_TYPE_NAME;

        virtual const std::string typeName() const { return Component::TYPE_NAME; }
        virtual const std::string baseTypeName() const { return Component::BASE_TYPE_NAME; }

        static const int32_t ADDED;
        static const int32_t REMOVED;
        static const int32_t REORDERED;
        static const int32_t nameLen;
        static const int32_t nullId;
        static const int32_t pathBufLen;

        //region Properties
        /**
         * Property 'meta'
         */
        Property<int32_t> meta = Property<int32_t>("meta", 1);

        //endregion

        //region Public members/methods
        /**
         * Get the path of this component from the root App.
         * The end of the path is denoted by null.  The list
         * is a shared static buffer.  Return null on error or
         * if not mounted under an Sys.app.
         */
        std::vector<Component>& path();

        /**
         * Lookup a child by name or return null.
         */
        Component* lookupByName(const std::string name);

        /**
         * Invoke an action which takes no arguments.
         */
        static void invokeVoid(Slot& slot);

        /**
         * Invoke an action which takes a boolean argument.
         */
        static void invokeBool(Slot& slot, bool arg);

        /**
         * Invoke an action which takes an int argument.
         */
        static void invokeInt(Slot& slot, int32_t arg);

        /**
         * Invoke an action which takes a long argument.
         */
        static void invokeLong(Slot& slot, int64_t arg);

        /**
         * Invoke an action which takes a float argument.
         */
        static void invokeFloat(Slot& slot, float arg);

        /**
         * Invoke an action which takes a double argument.
         */
        static void invokeDouble(Slot& slot, double arg);

        /**
         * Invoke an action which takes a Buf argument.
         */
        static void invokeBuf(Slot& slot, const Buf* arg);

        /**
         * Get a bool property using reflection.
         */
        static bool getBool(Slot& slot);

        /**
         * Get an integer 
         * byte, short, or int
         *  property using reflection.
         */
        static int32_t getInt(Slot& slot);

        /**
         * Get a long property using reflection.
         */
        static int64_t getLong(Slot& slot);

        /**
         * Get a float property using reflection.
         */
        static float getFloat(Slot& slot);

        /**
         * Get a double property using reflection.
         */
        static double getDouble(Slot& slot);

        /**
         * Get a Buf property using reflection.
         */
        static Buf* getBuf(Slot& slot);

        /**
         * Set a bool property using reflection.
         */
        void setBool(Slot& slot, bool val);

        /**
         * Set an integer 
         * byte, short, or int
         *  property using reflection.
         */
        void setInt(Slot& slot, int32_t val);

        /**
         * Set a long property using reflection.
         */
        void setLong(Slot& slot, int64_t val);

        /**
         * Set a float property using reflection.
         */
        void setFloat(Slot& slot, float val);

        /**
         * Set a double property using reflection.
         */
        void setDouble(Slot& slot, double val);

        /**
         * The changed method must be called when a slot is modified.
         * It is automatically called whenever setting a property
         * field with a primitive type 
         * bool, byte, short, int, and
         * float
         * .  However it must be manually called after updating
         * the contents of a Buf property.  If you override this
         * method, you MUST call super!
         */
        virtual void changed(Slot& slot);

        /**
         * Set property to a default value.  
         * Called when a link is removed from an input slot.
         */
        virtual void setToDefault(Slot& slot);

        /**
         * Callback when component is loaded into an app.  This occurs
         * before the start phase.  Only called if app is running.
         */
        virtual void loaded();

        /**
         * Callback when component is first started in an app.  This occurs
         * only after all components have had their loaded callback invoked. 
         * Only called if app is running
         */
        virtual void start();

        /**
         * Callback when component is first stopped in an app.  Only called
         * if app is running.
         */
        virtual void stop();

        /**
         * Execute is called once every scan using the
         * simple round-robin scan engine.
         */
        virtual void execute();

        /**
         * allowChildExecute returns false if child components of this
         * should not have execute
         * 
         *  called this app cycle.
         */
        virtual bool allowChildExecute();

        /**
         * Called on parent when a child event occurs.  Only called if app is
         * running.
         * 
         * Defined event types are:
         * 
         * REMOVED - notification that child has been removed from component.  
         *   Should always return 0.  Called after stop
         * 
         *  on child.
         * ADDED   - notification that child has been added to component.  
         *   Should always return 0.   Called prior to loaded
         * 
         *  / start
         * 
         *  on child.
         * REORDERED - notification that component's children have been reordered.  
         *   Should always return 0.   Called after the reordering is complete.
         *   Only called once per reorder event; child arg is always null.
         * 
         * Future event types may make use of return code.
         */
        virtual int32_t childEvent(int32_t eType, const Component& child);

        /**
         * Called on a child when a parent event occurs.  Only called if app is
         * running.
         * 
         * Defined event types are:
         * 
         * REMOVED - notification that child has been removed/unparented.  
         *   Should always return 0.  Called after stop
         * 
         *  on child.
         * ADDED   - notification this child has been parented.  
         *   Should always return 0.  Called prior to loaded
         * 
         *  / start
         * 
         *  on child.
         * 
         * Future event types may make use of return code.
         */
        virtual int32_t parentEvent(int32_t eType, const Component& parent);

        /**
         * Called on a component when a link event occurs.  Only called
         * if app is running.
         * 
         * Defined event types are:
         * 
         * REMOVED - link has been removed from the component.  
         *   Should always return 0
         * ADDED   - link has been added to the component.  
         *   Should always return 0. 
         * 
         * Future event types may make use of return code.
         */
        virtual int32_t linkEvent(int32_t eType, const Link* link);

        /**
         * Fire a tree changed event on this component by marking
         * the tree event bit for each watch's bitmask.
         */
        void fireTreeChanged();

        /**
         * Fire the links changed event on this component by marking
         * the link event bit for each watch's bitmask.
         */
        void fireLinksChanged();

        /**
         * Save the component's application information in
         * binary format to the output stream:
         * 
         *   appComp
         *   {
         *     u2      id
         *     u1      kitId
         *     u1      typeId
         *     str     name
         *     u2      parent
         *     u2      children
         *     u2      nextSibling
         *     val[]   configProps
         *     u1      ';' end marker
         *   }
         */
        void saveAppComp(OutStream* out);

        /**
         * Save the property values to the output stream.
         * Filter:
         *   0   = all
         *   'c' = config only
         *   'r' = runtime only
         */
        void saveProps(OutStream* out, int32_t filter);

        /**
         * Save a property value to the output stream.
         */
        void saveProp( OutStream* out, Slot& prop);

        /**
         * Load the component's configuration from a binary
         * format input stream - see saveAppComp
         * 
         *  for format.
         * We assume component id and type id have already been
         * read.  Return 0 on success or non-zero on error.
         */
        int32_t loadAppComp(InStream* in);

        /**
         * Load the property values to the output stream.
         * Filter:
         *   0   = all
         *   'c' = config only
         *   'r' = runtime only
         */
        void loadProps(InStream* in, int32_t filter);

        /**
         * Decode a value from the input stream
         * and set for the specified property.
         */
        void loadProp(InStream* in, Slot& slot);

        /**
         * Decode a value from the input stream
         * and invoke for the specified action.
         */
        void invokeAction(InStream* in, Slot& slot);

		void propagateLinksTo();

        uint16_t children = nullId;
        uint16_t id = 0 /* DEF */;
        Link* linksFrom;
        Link* linksTo;
        std::string name;
        uint16_t nextSibling = nullId;
        uint16_t parent = nullId;
        std::vector<uint8_t> watchFlags;
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
        /**
         * Method 'doSetBool' 
         * [NATIVE, PRIVATE]
         */
        static bool doSetBool(Slot& slot, bool val);

        /**
         * Method 'doSetInt' 
         * [NATIVE, PRIVATE]
         */
        static bool doSetInt(Slot& slot, int32_t val);

        /**
         * Method 'doSetLong' 
         * [NATIVE, PRIVATE]
         */
        static bool doSetLong(Slot& slot, int64_t val);

        /**
         * Method 'doSetFloat' 
         * [NATIVE, PRIVATE]
         */
        static bool doSetFloat(Slot& slot, float val);

        /**
         * Method 'doSetDouble' 
         * [NATIVE, PRIVATE]
         */
        static bool doSetDouble(Slot& slot, double val);

        /**
         * Method 'slotChanged' 
         * [PRIVATE]
         */
        void slotChanged(Slot& slot);


        //region Fields
		static std::vector<Component> pathBuf;
        //endregion
    //endregion
    }; // class Component

} // namespace sys

#endif
