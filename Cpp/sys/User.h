/*********************************************************
 * Header file for class 'User'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : User
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/

#ifndef SYS_USER_H
#define SYS_USER_H

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
#include "Buf.h"


namespace sys {

// Forward declaration
class User;

/**
 * User models a user account used for network authentication 
and authorization.  Users are stored as children of the UserService.
The user name is the component's name (limited to 7 chars).
 */
class User : public Component {

    //region Public members
    public:
        /**
         * Method '_iInit' 
         * []
         */
        User();

        /**
         * Virtual destructor
         */
        virtual ~User();

        static const std::string TYPE_NAME;
        static const std::string BASE_TYPE_NAME;

        virtual const std::string typeName() const { return User::TYPE_NAME; }
        virtual const std::string baseTypeName() const { return User::BASE_TYPE_NAME; }

        static const int32_t admInvoke;
        static const int32_t admRead;
        static const int32_t admWrite;
        static const int32_t opInvoke;
        static const int32_t opRead;
        static const int32_t opWrite;
        static const int32_t provApp;
        static const int32_t provKits;
        static const int32_t provSvm;
        static const int32_t ua;

        //region Properties
        /**
         * Property 'cred'
         */
        Property<Buf> cred = Property<Buf>("cred");

        /**
         * Property 'perm'
         */
        Property<int32_t> perm = Property<int32_t>("perm");

        /**
         * Property 'prov'
         */
        Property<uint8_t> prov = Property<uint8_t>("prov");

        //endregion

        //region Public members/methods
        /**
         * Return the permissions available .
         */
        int32_t permFor(const Component* c);

        /**
         * Return if this user has the specified permission 
         * on the given component.
         */
        bool has(const Component* c, int32_t hasPerm);

        /**
         * Can this user provisiong 
         * read/write
         *  the specified file.
         */
        bool canProv(const std::string uri);

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
    }; // class User

} // namespace sys

#endif
