// **********************************************************************
//
// Copyright (c) 2001
// Mutable Realms, Inc.
// Huntsville, AL, USA
//
// All Rights Reserved
//
// **********************************************************************

package IceInternal;

public final class ReferenceFactory
{
    public synchronized Reference
    create(Ice.Identity ident,
           String[] facet,
           int mode,
           boolean secure,
	   boolean compress,
	   String adapterId,
           Endpoint[] endpoints,
           RouterInfo routerInfo,
           LocatorInfo locatorInfo,
           Ice.ObjectAdapter reverseAdapter)
    {
        if(_instance == null)
        {
            throw new Ice.CommunicatorDestroyedException();
        }

        //
        // Create a new reference
        //
        Reference ref = new Reference(_instance, ident, facet, mode, secure, compress, adapterId,
				      endpoints, routerInfo, locatorInfo, reverseAdapter);

        //
        // If we already have an equivalent reference, use such equivalent
        // reference. Otherwise add the new reference to the reference
        // set.
        //
        // Java implementation note: A WeakHashMap is used to hold References,
        // allowing References to be garbage collected automatically. A
        // Reference serves as both key and value in the map. The
        // WeakHashMap class internally creates a weak reference for the
        // key, and we use a weak reference for the value as well.
        //
        java.lang.ref.WeakReference w = (java.lang.ref.WeakReference)_references.get(ref);
        if(w != null)
        {
            Reference r = (Reference)w.get();
            if(r != null)
            {
                ref = r;
            }
            else
            {
                _references.put(ref, new java.lang.ref.WeakReference(ref));
            }
        }
        else
        {
            _references.put(ref, new java.lang.ref.WeakReference(ref));
        }

        return ref;
    }

    public Reference
    create(String s)
    {
        final String delim = " \t\n\r";

        int beg;
        int end = 0;

        beg = StringUtil.findFirstNotOf(s, delim, end);
        if(beg == -1)
        {
            throw new Ice.ProxyParseException();
        }

        end = StringUtil.findFirstOf(s, delim + ":@", beg);
        if(end == -1)
        {
            end = s.length();
        }

        if(beg == end)
        {
            throw new Ice.ProxyParseException();
        }

        Ice.Identity ident = Ice.Util.stringToIdentity(s.substring(beg, end));
        java.util.ArrayList facet = new java.util.ArrayList();
        int mode = Reference.ModeTwoway;
        boolean secure = false;
        boolean compress = false;
	String adapter = "";

        while(true)
        {
            beg = StringUtil.findFirstNotOf(s, delim, end);
            if(beg == -1)
            {
                break;
            }

            if(s.charAt(beg) == ':' || s.charAt(beg) == '@')
            {
                break;
            }

            end = StringUtil.findFirstOf(s, delim + ":@", beg);
            if(end == -1)
            {
                end = s.length();
            }

            if(beg == end)
            {
                break;
            }

            String option = s.substring(beg, end);
            if(option.length() != 2 || option.charAt(0) != '-')
            {
                throw new Ice.ProxyParseException();
            }

            String argument = null;
            int argumentBeg = StringUtil.findFirstNotOf(s, delim, end);
            if(argumentBeg != -1 && s.charAt(argumentBeg) != '-')
            {
                beg = argumentBeg;
                end = StringUtil.findFirstOf(s, delim + ":@", beg);
                if(end == -1)
                {
                    end = s.length();
                    argument = s.substring(beg, end);
                }
            }

            //
            // If any new options are added here,
            // IceInternal::Reference::toString() must be updated as well.
            //
            switch(option.charAt(1))
            {
                case 'f':
                {
                    if(argument == null)
                    {
                        throw new Ice.EndpointParseException();
                    }

                    //
                    // TODO: Escape for whitespace and slashes.
                    //
                    int argBeg = 0;
                    while(argBeg < argument.length())
                    {
                        int argEnd = argument.indexOf('/', argBeg);
                        if(argEnd == -1)
                        {
                            facet.add(argument.substring(argBeg));
                        }
                        else
                        {
                            facet.add(argument.substring(argBeg, argEnd));
                            ++argEnd;
                        }
                        argBeg = argEnd;
                    }
                    break;
                }

                case 't':
                {
                    if(argument != null)
                    {
                        throw new Ice.EndpointParseException();
                    }
                    mode = Reference.ModeTwoway;
                    break;
                }

                case 'o':
                {
                    if(argument != null)
                    {
                        throw new Ice.EndpointParseException();
                    }
                    mode = Reference.ModeOneway;
                    break;
                }

                case 'O':
                {
                    if(argument != null)
                    {
                        throw new Ice.EndpointParseException();
                    }
                    mode = Reference.ModeBatchOneway;
                    break;
                }

                case 'd':
                {
                    if(argument != null)
                    {
                        throw new Ice.EndpointParseException();
                    }
                    mode = Reference.ModeDatagram;
                    break;
                }

                case 'D':
                {
                    if(argument != null)
                    {
                        throw new Ice.EndpointParseException();
                    }
                    mode = Reference.ModeBatchDatagram;
                    break;
                }

                case 's':
                {
                    if(argument != null)
                    {
                        throw new Ice.EndpointParseException();
                    }
                    secure = true;
                    break;
                }

                case 'c':
                {
                    if(argument != null)
                    {
                        throw new Ice.EndpointParseException();
                    }
                    compress = true;
                    break;
                }

                default:
                {
                    throw new Ice.ProxyParseException();
                }
            }
        }

        java.util.ArrayList endpoints = new java.util.ArrayList();
        if(beg != -1)
        {
	    if(s.charAt(beg) == ':')
	    {
                end = beg;

                while(end < s.length() && s.charAt(end) == ':')
                {
		    beg = end + 1;
		    
		    end = s.indexOf(':', beg);
		    if(end == -1)
		    {
			end = s.length();
		    }
		    
		    String es = s.substring(beg, end);
		    Endpoint endp = _instance.endpointFactoryManager().create(es);
		    endpoints.add(endp);
		}
	    }
	    else if(s.charAt(beg) == '@')
	    {
                beg = StringUtil.findFirstNotOf(s, delim, beg + 1);
                if(beg == -1)
                {
                    beg = end + 1;
                }

                end = StringUtil.findFirstOf(s, delim, beg);
                if(end == -1)
                {
                    end = s.length();
                }

                adapter = s.substring(beg, end);
                if(adapter.length() == 0)
                {
                    throw new Ice.ProxyParseException();
                }
	    }
	}

        Endpoint[] endp = new Endpoint[endpoints.size()];
        endpoints.toArray(endp);

        String[] fac = new String[facet.size()];
        facet.toArray(fac);

        RouterInfo routerInfo = _instance.routerManager().get(getDefaultRouter());
        LocatorInfo locatorInfo = _instance.locatorManager().get(getDefaultLocator());
        return create(ident, fac, mode, secure, compress, adapter, endp, routerInfo, locatorInfo, null);
    }

    public Reference
    create(Ice.Identity ident, BasicStream s)
    {
        //
        // Don't read the identity here. Operations calling this
        // constructor read the identity, and pass it as a parameter.
        //

        String[] facet = s.readStringSeq();

        int mode = (int)s.readByte();
        if(mode < 0 || mode > Reference.ModeLast)
        {
            throw new Ice.ProxyUnmarshalException();
        }

        boolean secure = s.readBool();

        boolean compress = s.readBool();

        Endpoint[] endpoints;
	String adapterId = "";

        int sz = s.readSize();
	if(sz > 0)
	{
	    endpoints = new Endpoint[sz];
	    for(int i = 0; i < sz; i++)
	    {
		endpoints[i] = _instance.endpointFactoryManager().read(s);
	    }
	}
	else
	{
	    endpoints = new Endpoint[0];
	    adapterId = s.readString();
	}

        RouterInfo routerInfo = _instance.routerManager().get(getDefaultRouter());
        LocatorInfo locatorInfo = _instance.locatorManager().get(getDefaultLocator());
        return create(ident, facet, mode, secure, compress, adapterId, endpoints, routerInfo, locatorInfo, null);
    }

    public synchronized void
    setDefaultRouter(Ice.RouterPrx defaultRouter)
    {
        _defaultRouter = defaultRouter;
    }

    public synchronized Ice.RouterPrx
    getDefaultRouter()
    {
        return _defaultRouter;
    }

    public synchronized void
    setDefaultLocator(Ice.LocatorPrx defaultLocator)
    {
        _defaultLocator = defaultLocator;
    }

    public synchronized Ice.LocatorPrx
    getDefaultLocator()
    {
        return _defaultLocator;
    }

    //
    // Only for use by Instance
    //
    ReferenceFactory(Instance instance)
    {
        _instance = instance;
    }

    synchronized void
    destroy()
    {
        if(_instance == null)
        {
            throw new Ice.CommunicatorDestroyedException();
        }

        _instance = null;
        _defaultRouter = null;
        _defaultLocator = null;
        _references.clear();
    }

    private Instance _instance;
    private Ice.RouterPrx _defaultRouter;
    private Ice.LocatorPrx _defaultLocator;
    private java.util.WeakHashMap _references = new java.util.WeakHashMap();
}
