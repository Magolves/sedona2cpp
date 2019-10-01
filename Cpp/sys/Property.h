#ifndef PROPERTY_H
#define PROPERTY_H

#include <vector>
#include <string>
#include <iostream>
#include <type_traits>
#include <typeinfo>
#include <assert.h>

#include "Slot.h"
#include "Units.h"

//using namespace std;

namespace sys {
	
class Component;
class Buf;

// Checks if T derives from Component
template <typename T>
using is_comp = std::integral_constant<bool, std::is_base_of<Component, T>::value>;


// Trait for for buffer type
template <typename T>
using is_buf = std::integral_constant<bool, std::is_same<T, Buf>::value>;

// Trait for slot type
template <typename T>
using is_slot = std::integral_constant<bool,
                        std::is_integral<T>::value ||
                        std::is_floating_point<T>::value ||
                        is_buf<T>::value>;

template <typename T>
using is_property = std::integral_constant<bool,
                        is_slot<T>::value ||
                        is_comp<T>::value>;

template <typename T>
using is_action = std::integral_constant<bool,
                        is_slot<T>::value ||
                        std::is_void<T>::value>;


template <class U, typename T>
union ActionPointer
{
    typedef typename std::conditional<std::is_void<T>::value, int, T>::type PropertyType;

    typedef void (U::*ActionPtr)(PropertyType);
    typedef void (U::*VoidActionPtr)();

    ActionPtr actionPtr;
    VoidActionPtr voidPtr;
};

template <class U, typename T>
struct SlotHolder : public Slot {
    static_assert(is_property<T>::value || is_action<T>::value, "Invalid slot type");
    static_assert(std::is_class<U>::value, "Class required");

	// HACK: Use int if is void type is passed ('void' action)
    typedef typename std::conditional<std::is_void<T>::value, int, T>::type PropertyType;

    typedef PropertyType (U::*Getter)() const;
    typedef PropertyType (U::*Setter)(PropertyType);
    typedef void (U::*ActionPtr)(PropertyType);
    typedef void (U::*VoidActionPtr)();

	/**
	 * Empty default constructor
	 */
    SlotHolder() = default;

    /**
     * Constructor for a property.
     */
    SlotHolder(std::string n, Getter g, Setter s, PropertyType defVal, int flgs = 0) : Slot(n, flgs) {
        this->getter = g;
        this->setter = s;
        this->defaultValue_ = defVal;
    }

    /**
     * Constructor for an action with an argument.
     */
    SlotHolder(std::string n, ActionPtr a, PropertyType defVal = PropertyType(), int flgs = 0) : Slot(n, flgs) {
        this->action.actionPtr = a;
        this->defaultValue_ = defVal;
    }

    /**
     * Constructor for a 'void' action.
     */
    SlotHolder(std::string n, VoidActionPtr a, int flgs = 0) : Slot(n, flgs) {
        this->action.voidPtr = a;
    }

    SlotHolder(SlotHolder&& sh) = default;

	virtual ~SlotHolder();

    // Enable getter if X is property type
    PropertyType get(const U* inst) const {
        assert(inst != nullptr);
        return (inst->*getter)();
    }

    // Enable setter if X is property type
    //template <typename X=T> typename std::enable_if<is_property<X>::value>::type
    PropertyType set(U* inst, PropertyType arg) {
        assert(inst != nullptr);
        return (inst->*setter)(arg);
    }

    // Enable action with arg if X is slot type
    //template <typename X=T> typename std::enable_if<is_slot<X>::value>::type
    void invoke(U* inst, PropertyType arg) {
        assert(inst != nullptr);
        (inst->*action.actionPtr)(arg);
    }

    void invokeWithDefault(U* inst) {
        assert(inst != nullptr);
        (inst->*action.actionPtr)(defaultValue_);
    }

    void invoke(U* inst) {
        assert(inst != nullptr);
        (inst->*action.voidPtr)();
    }

    /**
     * Method 'isProp'
     */
    virtual bool isProp() const {
        return  getter != nullptr;
    }

    /**
     * Method 'isAction'
     */
    virtual bool isAction() const {
        return action.actionPtr != nullptr;
    }
	
	std::string getTypeName() const {
		return std::string(typeid(T).name());
	}

    /**
     * Gets the default value of this instance.
     */
    constexpr T getDefaultValue() const {return defaultValue_;}

    /**
     * Sets the property to the default value. If this instance refers
     * to an action, this method does nothing.
     */
    void setToDefault(U* inst) {
        if (isProp()) {
            set(inst, defaultValue_);
        }
    }

    virtual std::string toString() const override {
		if (isProp()) {
			return this->name + ": " + getTypeName() + " = " + std::to_string(defaultValue_);
		} else {
			return this->name + "(" + getTypeName() + " = " + std::to_string(defaultValue_) + ")";
		}
    }

private:
    Getter getter = nullptr;
    Setter setter = nullptr;
    ActionPointer<U,T> action = {nullptr};
    PropertyType defaultValue_;
};

} // namespace


#endif