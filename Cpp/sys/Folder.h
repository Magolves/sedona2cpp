/*********************************************************
 * Header file for class 'Folder'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : Folder
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_FOLDER_H
#define SYS_FOLDER_H

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
class Folder;

/**
 * Folder is a component used to group other components.
 */
class Folder : public Component {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        Folder();

        /**
         * Virtual destructor
         */
        virtual ~Folder();

        static const std::string TYPE_NAME;
        static const std::string BASE_TYPE_NAME;

        virtual const std::string typeName() const { return Folder::TYPE_NAME; }
        virtual const std::string baseTypeName() const { return Folder::BASE_TYPE_NAME; }


        //region Properties
        //endregion

        //region Public members/methods
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
    }; // class Folder

} // namespace sys

#endif
