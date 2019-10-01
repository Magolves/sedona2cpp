/*********************************************************
 * Header file for class 'UserService'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : UserService
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_USERSERVICE_H
#define SYS_USERSERVICE_H

// STL incudes
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "FPHelper.h"
#include "Property.h"
#include "Service.h"


namespace sys {

// Forward declaration
class UserService;

/**
 * UserService stores the list of users as child components.
 */
class UserService : public Service {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        UserService();

        /**
         * Virtual destructor
         */
        virtual ~UserService();

        static const std::string TYPE_NAME;
        static const std::string BASE_TYPE_NAME;

        virtual const std::string typeName() const { return UserService::TYPE_NAME; }
        virtual const std::string baseTypeName() const { return UserService::BASE_TYPE_NAME; }


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
    }; // class UserService

} // namespace sys

#endif
