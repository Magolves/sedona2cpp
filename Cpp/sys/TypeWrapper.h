#ifndef TYPE_WRAPPER_H
#define TYPE_WRAPPER_H

#include <string>
#include <typeinfo>

#include "Type.h"

namespace sys {
template<typename T>
struct TypeWrapper : public Type {
	TypeWrapper(const std::vector<Slot*>& typeSlots) : Type(typeid(T).name(), typeSlots) {
		
	}
	
	static const std::string type_name;
	static const std::size_t size = sizeof(T);
};


} // namespace
#endif
