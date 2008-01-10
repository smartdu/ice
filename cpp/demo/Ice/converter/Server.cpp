// **********************************************************************
//
// Copyright (c) 2003-2007 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

#include <Ice/Ice.h>
#include <GreetI.h>

using namespace std;

class ConverterServer : public Ice::Application
{
public:

    virtual int run(int, char*[]);
};

int
main(int argc, char* argv[])
{
    ConverterServer app;
    return app.main(argc, argv, "config.server");
}

int
ConverterServer::run(int argc, char* argv[])
{
    if(argc > 1)
    {
        cerr << appName() << ": too many arguments" << endl;
        return EXIT_FAILURE;
    }

    Ice::ObjectAdapterPtr adapter = communicator()->createObjectAdapter("Greet");
    adapter->add(new GreetI, communicator()->stringToIdentity("greet"));
    adapter->activate();
    communicator()->waitForShutdown();
    return EXIT_SUCCESS;
}
