#
# PReDoc - an editor for proof-reading digital documents
# Copyright (C) 1998 by Mark-André Hopf
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#

all: config client server

config:
	cd conf; configure
	
server: config client
	cd server; make

client: config
	cd client; make

clean::
	-cd server; make clean ; rm Makefile
	-cd client; make clean ; rm Makefile
	-rm -f `find . -name "*.class"`
	-rm -f `find . -name "*~"`
	-rm -f `find . -name "DEADJOE"`
	-rm -f `find . -name "*.bak"`
	-rm -f `find . -name "*.jar"`
	-rm -f conf/config.cache
	-rm -f conf/config.log 
	-rm -f conf/config.status 
	-rm -f conf/confdefs.h
	-rm -f conf/MakefilePrg
	-rm -f conf/Makefile.conf
	-rm -rf conf/conftestdir
