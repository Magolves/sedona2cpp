#ifndef SLOTWRAPPER_H
#define SLOTWRAPPER_H

#pragma once

#include <vector>
#include <string>
#include <iostream>
#include <type_traits>
#include <typeinfo>
#include <utility>
#include <assert.h>

#include "Units.h"
#include "Slot.h"
#include "Type.h"

using namespace std;

namespace sys {

class Component;
class Buf;

typedef uint8_t SlotFlags;
typedef uint32_t SlotId;



// Template aliases (C++ 14)

// Checks if T derives from Component
template <typename T>
using is_comp = std::integral_constant<bool, std::is_base_of<Component, T>::value>;


// Trait for for buffer type
template <typename T>
using is_buf = std::integral_constant<bool, std::is_same<T, Buf>::value>;

template <typename T>
using is_bool = std::integral_constant<bool, std::is_same<bool, T>::value>;


template <typename T>
using is_nullable_type = std::integral_constant<bool,
                        is_bool<T>::value ||
                        std::is_floating_point<T>::value>;


template <typename T>
class Nullable {
	static_assert(is_nullable_type<T>::value, "Only bool, float or double are valid");
	
public:
	// Default constructor
	Nullable() {
		value_ = T();
		isNull_ = true;
	}
	
	// Default constructor with value
	Nullable(T v) {
		value_ = v;
		isNull_ = false;
	}

	// Copy constructor
	Nullable(const Nullable<T>& nValue) {
		value_ = nValue.value_;
		isNull_ = nValue.isNull_;		
	}
	
	// Assignment operators
	Nullable<T>& operator = (const Nullable<T>& nValue) {
		if (this != &nValue) {		
			value_ = nValue.value_;
			isNull_ = nValue.isNull_;
		}
		return *this;
	}

	Nullable<T>& operator = (const T v) {
		value_ = v;
		isNull_ = false;
		return *this;
	}

	// Comparison operators
	bool operator==(const Nullable<T>& nValue) const {
        return isNull_ == nValue.isNull_ && value_ == nValue.value_;
    }

	bool operator!=(const Nullable<T>& nValue) const {
        return !(*this == nValue);
    }
	
	bool operator==(nullptr_t) const {
        return isNull_;
    }

	bool operator!=(nullptr_t) const {
        return !isNull_;
    }
	
	bool operator==(const T scalar) const {
        return !isNull_ && value_ == scalar;
    }
	
	bool operator!=(const T scalar) const {
        return !(*this.value_ == scalar);
    }

	friend bool operator== (const T a, const Nullable<T> nValue) {
		return nValue.isNull_ ? false : a == nValue.value_;
	}
	
	friend bool operator== (const nullptr_t a, const Nullable<T> nValue) {
		return nValue.isNull_;
	}
	
	// Arithmetik  ops
	Nullable<T>& operator += (const T scalar) {
		this->value_ += scalar;
		return *this;
	}
	
	Nullable<T>& operator -= (const T scalar) {
		this->value_ -= scalar;
		return *this;
	}
	
	Nullable<T>& operator *= (const T scalar) {
		this->value_ *= scalar;
		return *this;
	}
	
	Nullable<T>& operator /= (const T scalar) {
		this->value_ /= scalar;
		return *this;
	}
	
	T operator + (const Nullable<T>& nValue) {
		return value_ + nValue.value_;
	}
	
	T operator - (const Nullable<T>& nValue) {
		return value_ - nValue.value_;
	}
	
	T operator * (const Nullable<T>& nValue) {
		return value_ * nValue.value_;
	}

	T operator / (const Nullable<T>& nValue) {
		return value_ / nValue.value_;
	}
	
	friend T operator + (const T a, const Nullable<T>& nValue) {
		return a + nValue.value_;
	}
	
	friend T operator - (const T a, const Nullable<T>& nValue) {		
		return a - nValue.value_;
	}
	
	friend T operator * (const T a, const Nullable<T>& nValue) {		
		return a * nValue.value_;
	}
	
	friend T operator / (const T a, const Nullable<T>& nValue) {
		return a / nValue.value_;
	}

	// Type conversion operator
	explicit operator T() const {
        return value_;
    }
	
	// Getters/setters
	T value() const {
		return value_;
	}
	
	bool isNull() const {
		return isNull_;
	}
	
	void setNull(){
		isNull_ = true;
	}

private:
	T value_ = T();
	bool isNull_ = false;
};


// Predefined nullable types
typedef Nullable<bool> nbool;
typedef Nullable<float> nfloat;
typedef Nullable<double> ndouble;


// Type traits for slot, property and action
template <typename T>
using is_nullable_slot = std::integral_constant<bool, 
	std::is_same<nbool, T>::value ||
	std::is_same<nfloat, T>::value ||
	std::is_same<ndouble, T>::value
	>;


// Trait for slot type
template <typename T>
using is_slot = std::integral_constant<bool,
                        std::is_integral<T>::value ||
                        is_nullable_slot<T>::value ||
                        is_buf<T>::value>;

template <typename T>
using is_property = std::integral_constant<bool,
                        is_slot<T>::value ||
                        is_comp<T>::value>;

template <typename T>
using is_action = std::integral_constant<bool,
                        std::is_integral<T>::value ||
						is_bool<T>::value ||
                        std::is_floating_point<T>::value ||
                        std::is_void<T>::value>;


// Union for ActionPtr with arg and void
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
struct SlotWrapper : public Slot {
    static_assert(is_property<T>::value || is_action<T>::value, "Invalid slot type");
    static_assert(std::is_class<U>::value, "Class required");

	// HACK: Use int if is void type is passed ('void' action)
    typedef typename std::conditional<std::is_void<T>::value, int, T>::type PropertyType;
	// Typedefs
    typedef PropertyType (U::*Getter)() const;
    typedef PropertyType (U::*Setter)(PropertyType);
    typedef void (U::*ActionPtr)(PropertyType);
    typedef void (U::*VoidActionPtr)();

	/**
	 * Empty default constructor
	 */
    SlotWrapper() = default;

    /**
     * Constructor for a property.
     */
	SlotWrapper(std::string n, Getter g, Setter s, PropertyType defVal, SlotFlags flgs = 0) : Slot(n, flgs) {
        this->getter_ = g;
        this->setter_ = s;
        this->defaultValue_ = defVal;
    }

    /**
     * Constructor for an action with an argument.
     */
    SlotWrapper(std::string n, ActionPtr a, PropertyType defVal = PropertyType(), SlotFlags flgs = 0) : Slot(n, flgs) {
        this->action_.actionPtr = a;
        this->defaultValue_ = defVal;
    }


    /**
     * Constructor for a 'void' action.
     */
    SlotWrapper(std::string n, VoidActionPtr a, SlotFlags flgs = 0) : Slot(n, flgs) {
        this->action_.voidPtr = a;
    }

    SlotWrapper(SlotWrapper&& slot) = default;

	virtual ~SlotWrapper() {}		
		   
    /**
     * Gets the property value of 'inst'
     */
    PropertyType get(const U* inst) const {
        assert(inst != nullptr);
        return (inst->*getter_)();
    }

    /**
     * Sets the property value of 'inst'
     */
    PropertyType set(U* inst, PropertyType arg) {
        assert(inst != nullptr);
        return (inst->*setter_)(arg);
    }

    // Enable action with arg if X is slot type
    template <typename X = T>
    typename std::enable_if<is_action<X>::value, void>::type
    invoke(U* inst, PropertyType arg) {
        assert(inst != nullptr);
        (inst->*action_.actionPtr)(arg);
    }

    template <typename X = T>
    typename std::enable_if<is_action<X>::value, void>::type
    invokeWithDefault(U* inst) {
        assert(inst != nullptr);
        (inst->*action_.actionPtr)(defaultValue_);
    }

    template <typename X = T>
    typename std::enable_if<is_action<X>::value, void>::type
    invoke(U* inst) {
        assert(inst != nullptr);
        (inst->*action_.voidPtr)();
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

    /**
     * Method 'isProp' from Slot.
     */
    virtual bool isProp() const {
        return  getter_ != nullptr;
    }

    /**
     * Method 'isAction' from Slot.
     */
    virtual bool isAction() const {
        return action_.actionPtr != nullptr;
    }
	
	std::string getTypeName() const {
		return std::string(typeid(T).name());
	}
	
	template <typename X=T> 
	typename std::enable_if<is_integral<X>::value, std::string>::type
    toString() const {
		const std::string prefix = "[" + std::to_string(id) + "] " + this->name + ": " + getTypeName() + " = ";
		if (isProp()) {
			return prefix + std::to_string(defaultValue_);
		} else {
			return prefix + "(" + std::to_string(defaultValue_) + ")";
		}
    }
	
	template <typename X=T> 
	typename std::enable_if<is_nullable_type<X>::value, std::string>::type
    toString() const {
		const std::string prefix = "[" + std::to_string(id) + "] " + this->name + ": " + getTypeName() + " = ";
		if (isProp()) {
			return prefix + std::to_string(defaultValue_.value_);
		} else {
			return prefix + "(" + std::to_string(defaultValue_) + ")";
		}
    }

private:
    Getter getter_ = nullptr;
    Setter setter_ = nullptr;
    ActionPointer<U,T> action_ = {nullptr};
    PropertyType defaultValue_;	
};

/**
 * Specialized slot vector whichs assigns the slot ids via the initializer list .
 */
class SlotVector : public std::vector<sys::Slot*> {
	public:
		/**
		 * Delete default constructor to force construction via init list.
		 */
		SlotVector() = delete;
		
		/**
		 * Constructor with initializer list
		 */
		SlotVector(std::initializer_list<sys::Slot*> slots) {
			for(auto it = slots.begin(); it != slots.end(); ++it) {
				(*it)->id = std::distance(slots.begin(), it);
			}
			this->assign(slots);
		}
};

} // namespace


#endif
