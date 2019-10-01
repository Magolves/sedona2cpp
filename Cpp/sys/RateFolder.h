/*********************************************************
 * Header file for class 'RateFolder'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : RateFolder
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_RATEFOLDER_H
#define SYS_RATEFOLDER_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "Folder.h"


namespace sys {

// Forward declaration
class RateFolder;

/**
 * RateFolder is a folder that controls how often its children execute.
It can be used to implement an app with components that execute at
different rates.
 */
class RateFolder : public Folder {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        RateFolder();

        /**
         * Virtual destructor
         */
        virtual ~RateFolder();

        static const std::string TYPE_NAME;
        static const std::string BASE_TYPE_NAME;

        virtual const std::string typeName() const { return RateFolder::TYPE_NAME; }
        virtual const std::string baseTypeName() const { return RateFolder::BASE_TYPE_NAME; }


        //region Properties
        /**
         * Property 'appCyclesToSkip'
         */
        Property<int32_t> appCyclesToSkip = Property<int32_t>("appCyclesToSkip", 0);

        //endregion

        //region Public members/methods
        /**
         * Override loaded
         * 
         *  to init execCount.
         */
        virtual void loaded() override ;

        /**
         * Calculate when to allow children to execute.
         */
        virtual bool allowChildExecute() override;
        //endregion
    //endregion
    protected:

    //region Protected members/methods

        //region Fields
        int32_t execCount = 0;
        //endregion
    //endregion

    //region Private members/methods
    private:

        //region Fields
        //endregion
    //endregion
    }; // class RateFolder

} // namespace sys

#endif
