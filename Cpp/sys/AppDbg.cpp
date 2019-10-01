/*********************************************************
 * Implementation for class 'AppDbg'.
 * (C) Robert Bosch GmbH 2019
 * Tag      : $Id$
 * Namespace: sys
 * Class    : AppDbg
 * Generated: Mon Sep 02 20:20:32 CEST 2019
 *********************************************************/


/* STL includes */
#include <vector>
#include <iterator>
#include <iostream>
#include <fstream>
#include <sstream>
#include <stdint.h>

#include "AppDbg.h"
#include "Slot.h"
#include "Component.h"
#include "OutStream.h"

// NOTE: Set usingStd="false" to get full-qualified STL types
using namespace std;

namespace sys {

/**
 * Constructor for 'AppDbg 
 * _iInit
 * '
 */
AppDbg::AppDbg() {
    ;
    /* Init virt InitVirt sys::AppDbg */;
    ;
    (this->overruns) = 0;
    (this->scanCyclesPerUpdate) = 1000;
}


/**
 * Implementation of method 'start'
 */
void AppDbg::start() {
    resetValues();
}


/**
 * Detect if enable has changed from false to true, if so
 * reset all the calculation variables
 */
void AppDbg::changed(const Slot* slot) {
    if ((slot->id) == enable) {
        resetValues();
    }
    changed(slot);
}


/**
 * Calculate and cache previous scan's times every scan
 */
void AppDbg::execute() {
    if (!isSteadyState()) {
    }
    if (!(this->enable)) {
    }
    (this->tmpExecTime) = (static_cast<float>(((Sys::getApp()->lastStartWork) - (Sys::getApp()->lastStartExec))) * 1.0E-6f);
    (this->tmpWorkTime) = (static_cast<float>(((Sys::getApp()->lastEndWork) - (Sys::getApp()->lastStartWork))) * 1.0E-6f);
    (this->tmpScanTime) = (static_cast<float>(((Sys::getApp()->newStartExec) - (Sys::getApp()->lastStartExec))) * 1.0E-6f);
    (this->tmpExecTimeMax) = (((this->tmpExecTime) > (this->tmpExecTimeMax)) ? (this->tmpExecTime) : (this->tmpExecTimeMax));
    (this->tmpExecTimeMin) = (((this->tmpExecTime) < (this->tmpExecTimeMin)) ? (this->tmpExecTime) : (this->tmpExecTimeMin));
    (this->tmpWorkTimeMax) = (((this->tmpWorkTime) > (this->tmpWorkTimeMax)) ? (this->tmpWorkTime) : (this->tmpWorkTimeMax));
    (this->tmpWorkTimeMin) = (((this->tmpWorkTime) < (this->tmpWorkTimeMin)) ? (this->tmpWorkTime) : (this->tmpWorkTimeMin));
    (this->tmpScanTimeMax) = (((this->tmpScanTime) > (this->tmpScanTimeMax)) ? (this->tmpScanTime) : (this->tmpScanTimeMax));
    (this->tmpScanTimeMin) = (((this->tmpScanTime) < (this->tmpScanTimeMin)) ? (this->tmpScanTime) : (this->tmpScanTimeMin));
    if ((this->tmpScanTime) > (static_cast<float>((Sys::getApp()->scanPeriod)) + 1.0f)) {
        overruns = (this->overruns) + 1;
    }
    if ((this->print)) {
        nl();
    }
    if (((Sys::getApp()->cycleCount) % static_cast<int64_t>((this->scanCyclesPerUpdate))) == 0) {
        (this->execTime) = (this->tmpExecTime);
        (this->workTime) = (this->tmpWorkTime);
        (this->scanTime) = (this->tmpScanTime);
        execMin = (this->tmpExecTimeMin);
        execMax = (this->tmpExecTimeMax);
        workMin = (this->tmpWorkTimeMin);
        workMax = (this->tmpWorkTimeMax);
        scanMin = (this->tmpScanTimeMin);
        scanMax = (this->tmpScanTimeMax);
        if ((this->first)) {
            (this->first) = false;
            execMinMin = (this->execMin);
            execMaxMax = (this->execMax);
            workMinMin = (this->workMin);
            workMaxMax = (this->workMax);
            scanMinMin = (this->scanMin);
            scanMaxMax = (this->scanMax);
        }
         else  {
            execMinMin = ((this->execMin) < (this->execMinMin)) ? (this->execMin) : (this->execMinMin);
            execMaxMax = ((this->execMax) > (this->execMaxMax)) ? (this->execMax) : (this->execMaxMax);
            workMinMin = ((this->workMin) < (this->workMinMin)) ? (this->workMin) : (this->workMinMin);
            workMaxMax = ((this->workMax) > (this->workMaxMax)) ? (this->workMax) : (this->workMaxMax);
            scanMinMin = ((this->scanMin) < (this->scanMinMin)) ? (this->scanMin) : (this->scanMinMin);
            scanMaxMax = ((this->scanMax) > (this->scanMaxMax)) ? (this->scanMax) : (this->scanMaxMax);
        }
        execAvg = (this->execTimeRunningTotal) / static_cast<float>((this->scanCyclesPerUpdate));
        workAvg = (this->workTimeRunningTotal) / static_cast<float>((this->scanCyclesPerUpdate));
        scanAvg = (this->scanTimeRunningTotal) / static_cast<float>((this->scanCyclesPerUpdate));
        (this->execTimeRunningTotal) = 0.0f;
        (this->workTimeRunningTotal) = 0.0f;
        (this->scanTimeRunningTotal) = 0.0f;
        (this->tmpExecTimeMax) = 0.0f;
        (this->tmpExecTimeMin) = static_cast<float>((Sys::maxInt));
        (this->tmpWorkTimeMax) = 0.0f;
        (this->tmpWorkTimeMin) = static_cast<float>((Sys::maxInt));
        (this->tmpScanTimeMax) = 0.0f;
        (this->tmpScanTimeMin) = static_cast<float>((Sys::maxInt));
    }
    (this->execTimeRunningTotal) += (this->tmpExecTime);
    (this->workTimeRunningTotal) += (this->tmpWorkTime);
    (this->scanTimeRunningTotal) += (this->tmpScanTime);
}


/**
 * Reset the max value calculations and the overruns value
 */
void AppDbg::resetValues() {
    (this->first) = true;
    execAvg = 0.0f;
    execMin = 0.0f;
    execMax = 0.0f;
    execMinMin = static_cast<float>((Sys::maxInt));
    execMaxMax = 0.0f;
    (this->execTimeRunningTotal) = 0.0f;
    workAvg = 0.0f;
    workMin = 0.0f;
    workMax = 0.0f;
    workMinMin = static_cast<float>((Sys::maxInt));
    workMaxMax = 0.0f;
    (this->workTimeRunningTotal) = 0.0f;
    scanAvg = 0.0f;
    scanMin = 0.0f;
    scanMax = 0.0f;
    scanMinMin = static_cast<float>((Sys::maxInt));
    scanMaxMax = 0.0f;
    (this->scanTimeRunningTotal) = 0.0f;
    overruns = 0;
    (this->tmpExecTimeMin) = static_cast<float>((Sys::maxInt));
    (this->tmpWorkTimeMin) = static_cast<float>((Sys::maxInt));
    (this->tmpScanTimeMin) = static_cast<float>((Sys::maxInt));
    (this->tmpExecTimeMax) = 0.0f;
    (this->tmpWorkTimeMax) = 0.0f;
    (this->tmpScanTimeMax) = 0.0f;
    (this->tmpExecTime) = 0.0f;
    (this->tmpWorkTime) = 0.0f;
    (this->tmpScanTime) = 0.0f;
}


/**
 * Copy constructor for 'AppDbg'
 */
/**
 * Move constructor for 'AppDbg'
 */
/**
 * Destructor
 */
AppDbg::~AppDbg() {}

const string AppDbg::TYPE_NAME = "sys::AppDbg";
const string AppDbg::BASE_TYPE_NAME = "sys::Component";

} // namespace sys
