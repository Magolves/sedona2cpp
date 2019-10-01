/*********************************************************
 * Configuration header for Sedona/C++
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Generated: Mon Sep 02 19:13:37 CEST 2019
 *********************************************************/

#ifndef CONFIG_H
#define CONFIG_H

#pragma once

// platform specific header files
#ifdef _WIN32

#elif _WIN64


#elif __APPLE__ || __MACH__

#elif __linux__
#include <unistd.h>
#elif __FreeBSD__

#elif __unix || __unix__
#include <unistd.h>
#else
#error "Unknown platform configuration"
#endif

#if __cplusplus > 201402L
#define CPP_STD_17
#elif __cplusplus > 199711L
#define CPP_STD_11
#endif


#include <chrono>


/* Timer resolution. If CFG_HIGH_RES_TIMER is defined, sys::ticks are measured in nanoseconds;otherwise in milliseconds.
 */
#define CFG_HIGH_RES_TIMER


//////////////////// Chrono ///////////////////////////
using Clock = std::chrono::high_resolution_clock;

#ifdef CFG_HIGH_RES_TIMER
using Ticks = std::chrono::nanoseconds;
#else
using Ticks = std::chrono::milliseconds;
#endif

template<class Duration>
using TimeStamp = std::chrono::time_point<Clock, Duration>;

// Took code from https://stackoverflow.com/questions/105252/how-do-i-convert-between-big-endian-and-little-endian-values-in-c
template <typename T>
void SwapEndian(T& arg)
{
#if defined(_WIN32) || defined(_WIN64)
    uint8_t* byteArray = reinterpret_cast<uint8_t*>(&arg);
    for (long i = 0; i < static_cast<long>(sizeof(arg) / 2); i++) {
        std::swap(byteArray[sizeof(arg) - 1 - i], byteArray[i]);
    }
#endif
}

#endif // guard
