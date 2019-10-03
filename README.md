# What is _sedona2cpp_
Enhance the Sedona suite with a cross-compiler to C++.

Sedona is a simple programming language intended for small systems and low footprint. 

Please refer to https://www.sedona-alliance.org/resources.htm for further details.

The goal of this project ist to implement the "translate" function which is intended to transform Sedona code into another programming language.
In this project the focus lies on C++.

# Challenges
Although the C++ language is very powerful, there are some Sedona
features which may cause some headache
* Reflection. Sedona offers a database containing all modules (AKA 'Kits') including their types and members (AKA 'Slots')
* Some types (bool, float and double) can be _null_
