// **********************************************************************
//
// Copyright (c) 2003-2016 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

#include <Ice/Ice.h>
#include <TestCommon.h>
#include <TestI.h>

DEFINE_TEST("server")

using namespace std;

int
run(int, char**, const Ice::CommunicatorPtr& communicator)
{
    communicator->getProperties()->setProperty("TestAdapter.Endpoints", getTestEndpoint(communicator, 0) + ":udp");
    Ice::ObjectAdapterPtr adapter = communicator->createObjectAdapter("TestAdapter");
    Ice::Identity id = Ice::stringToIdentity("communicator");
    adapter->add(ICE_MAKE_SHARED(RemoteCommunicatorI), id);
    adapter->activate();

    TEST_READY

    // Disable ready print for further adapters.
    communicator->getProperties()->setProperty("Ice.PrintAdapterReady", "0");

    communicator->waitForShutdown();
    return EXIT_SUCCESS;
}

int
main(int argc, char* argv[])
{
#ifdef ICE_STATIC_LIBS
    Ice::registerIceSSL();
#endif
    try
    {
        Ice::InitializationData initData = getTestInitData(argc, argv);
        Ice::CommunicatorHolder ich = Ice::initialize(argc, argv, initData);
        return run(argc, argv, ich.communicator());
    }
    catch(const Ice::Exception& ex)
    {
        cerr << ex << endl;
        return EXIT_FAILURE;
    }
}
