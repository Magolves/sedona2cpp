##########################################################
# Makefile for kit 'sys'
# (C) Robert Bosch GmbH 2019
# Tag      : $Id$
# Namespace: sys
# Generated: Mon Sep 02 20:20:32 CEST 2019
##########################################################/

SRCS = Component.cpp \
       App.cpp \
       Buf.cpp \
       InStream.cpp \
       BufInStream.cpp \
       OutStream.cpp \
       BufOutStream.cpp \
       Err.cpp \
       FileInStream.cpp \
       FileOutStream.cpp \
       Folder.cpp \
       Kit.cpp \
       Link.cpp \
       Log.cpp \
       NullInStream.cpp \
       NullOutStream.cpp \
       Service.cpp \
       PlatformService.cpp \
       RateFolder.cpp \
       Slot.cpp \
       StdOutStream.cpp \
       Sys.cpp \
	   Units.cpp \
       User.cpp \
       UserService.cpp \
       Watch.cpp

OBJS = Component.o \
       App.o \
       Buf.o \
       InStream.o \
       BufInStream.o \
       OutStream.o \
       BufOutStream.o \
       Err.o \
       FileInStream.o \
       FileOutStream.o \
       Folder.o \
       Kit.o \
       Link.o \
       Log.o \
       NullInStream.o \
       NullOutStream.o \
       Service.o \
       PlatformService.o \
       RateFolder.o \
       Slot.o \
       StdOutStream.o \
       Sys.o \
	   Units.o \
       User.o \
       UserService.o \
       Watch.o

INCLUDES = -I. -I..
CPPFLAGS = -std=c++17 -Wall -Wextra -Wnon-virtual-dtor -pedantic
# The library to build
MAIN = libsys.a

all: $(MAIN)

$(MAIN): $(OBJS)
	ar -rs $(MAIN) $(OBJS) $(LIBS)
	ranlib $(MAIN)
.cpp.o:
	$(CXX) $(CPPFLAGS) $(INCLUDES) -c $< -o $@

clean:
	rm $(MAIN)
	rm $(OBJS) 

# Dependencies
Component.o: Component.h Link.h Slot.h Component.h InStream.h OutStream.h Buf.h Property.h
App.o: App.h Link.h Slot.h Component.h InStream.h OutStream.h Log.h Watch.h Buf.h PlatformService.h Service.h Property.h
AppDbg.o: AppDbg.h Slot.h Component.h
Buf.o: Buf.h
InStream.o: InStream.h
BufInStream.o: BufInStream.h InStream.h Buf.h
OutStream.o: OutStream.h
BufOutStream.o: BufOutStream.h OutStream.h Buf.h
Err.o: Err.h
FileInStream.o: FileInStream.h InStream.h
FileOutStream.o: FileOutStream.h OutStream.h Buf.h
Folder.o: Folder.h Component.h
Kit.o: Kit.h
Link.o: Link.h InStream.h OutStream.h
Log.o: Log.h OutStream.h
NullInStream.o: NullInStream.h InStream.h
NullOutStream.o: NullOutStream.h OutStream.h
Service.o: Service.h Component.h
PlatformService.o: PlatformService.h Buf.h Service.h Property.h
RateFolder.o: RateFolder.h Folder.h Property.h
Slot.o: Slot.h
StdOutStream.o: StdOutStream.h OutStream.h
Sys.o: Sys.h Log.h OutStream.h App.h NullInStream.h StdOutStream.h Kit.h NullOutStream.h
User.o: User.h Component.h Buf.h Property.h
UserService.o: UserService.h Service.h Property.h
Watch.o: Watch.h
