# **********************************************************************
#
# Copyright (c) 2003-2016 ZeroC, Inc. All rights reserved.
#
# This copy of Ice is licensed to you under the terms described in the
# ICE_LICENSE file included in this distribution.
#
# **********************************************************************

$(project)_libraries := IcePy

IcePy_target		:= python-module
IcePy_targetname	:= IcePy
IcePy_targetdir		:= $(lang_srcdir)/python
IcePy_installdir	:= $(install_pythondir)
IcePy_cppflags  	:= -I$(project) -I$(top_srcdir)/cpp/include -I$(top_srcdir)/cpp/include/generated $(python_cppflags)
IcePy_ldflags		:= $(python_ldflags)
IcePy_dependencies	:= IceSSL Ice Slice IceUtil

projects += $(project)
srcs:: $(project)