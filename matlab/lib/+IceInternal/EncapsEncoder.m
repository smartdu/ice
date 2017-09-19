%{
**********************************************************************

Copyright (c) 2003-2017 ZeroC, Inc. All rights reserved.

This copy of Ice is licensed to you under the terms described in the
ICE_LICENSE file included in this distribution.

**********************************************************************
%}

classdef (Abstract) EncapsEncoder < handle
    methods
        function obj = EncapsEncoder(os, encaps)
            obj.os = os;
            obj.encaps = encaps;
            obj.typeIdIndex = 0;
        end

        function r = writeOptional(obj, tag, format)
            r = false;
        end

        function writePendingValues(obj)
            %
            % Overridden for the 1.0 encoding, not necessary for subsequent encodings.
            %
        end
    end
    methods(Abstract)
        writeValue(obj, v)
        startInstance(obj, sliceType, slicedData)
        endInstance(obj)
        startSlice(obj, typeId, compactId, last)
        endSlice(obj)
    end
    methods(Access=protected)
        function r = registerTypeId(obj, typeId)
            if isempty(obj.typeIdMap) % Lazy initialization
                obj.typeIdMap = containers.Map('KeyType', 'char', 'ValueType', 'int32');
            end

            if obj.typeIdMap.isKey(typeId)
                r = obj.typeIdMap(typeId);
            else
                obj.typeIdIndex = obj.typeIdIndex + 1;
                obj.typeIdMap(typeId) = obj.typeIdIndex;
                r = -1;
            end
        end
    end
    properties(Access=protected)
        os
        encaps
    end
    properties(Access=private)
        typeIdMap
        typeIdIndex
    end
end
