#!/bin/sh

#############################################################################
# 
# PReDoc Installation
# Written 1998 by Mark-André Hopf
# Copyright (C) 1998 by Mark-André Hopf
#
#############################################################################

# terminal configuration
#------------------------

invers_on="[7m"
invers_off="[27m"
bold_on="[1m"
bold_off="[22m"

tcc_output=Makefile.conf

flag_client="yes"
flag_server="yes"
flag_exec="$HOME/bin"
flag_int="$HOME/lib/predoc"
flag_doc="$HOME/predoc"

make_dir() {
	md_path=""
	for md_piece in `echo "$1" | sed "s/\// /g"` ; do
		md_path="${md_path}/${md_piece}"
		if test -a $md_path -a ! -d $md_path ; then
			echo "Error: creation of directory "
			echo "         ${bold_on}${1}${bold_off}"
			echo "       is blocked by a file named"
			echo "         ${bold_on}${md_path}${bold_off}"
			return 0
		fi
		if test ! -d $md_path ; then
			mkdir $md_path
		fi
	done
	return 1
}

install_predoc() {
	echo "Here we go..."
	make_dir $flag_exec
	if test $? -eq 0 ; then
		return 1
	fi
	make_dir $flag_int
	if test $? -eq 0 ; then
		return 1
	fi
	make_dir $flag_doc
	if test $? -eq 0 ; then
		return 1
	fi
	return 0
}

# install_predoc

#############################################################################
# main menu
#############################################################################

	while true ; do
		clear
cat << EOF

  ${bold_on}Where would you like to install PReDoc ?${bold_off}

  [1] Install PReDoc Client   : ${flag_client}
  [2] Install PReDoc Server   : ${flag_server}

  [3] Path for executables    : ${flag_exec}
  [4] Path for internal files : ${flag_int}
  [5] Path for document files : ${flag_doc}

  [6] Start installation

  [0] Quit

EOF
		echo -n "$bold_on  Please enter a number:$bold_off "
		read inp
		case $inp in
			0)	break;;
			1)	if test $flag_client = "yes" ; then
						flag_client="no"
					else
						flag_client="yes"
					fi;;
			2)	if test $flag_server = "yes" ; then
						flag_server="no"
					else
						flag_server="yes"
					fi;;
			3)	echo " "
					echo -n "  ${bold_on}Enter the new path for executables:${bold_off} "
					read inp
					if ! test -z $inp ; then
						flag_exec=$inp
					fi;;
			4)	echo " "
					echo -n "  ${bold_on}Enter the new path for internal files:${bold_off} "
					read inp
					if ! test -z $inp ; then
						flag_int=$inp
					fi;;
			5)	echo " "
					echo -n "  ${bold_on}Enter the new path for document files:${bold_off} "
					read inp
					if ! test -z $inp ; then
						flag_doc=$inp
					fi;;
			6)  echo " "
					echo -n "  ${bold_on}Do you really want to install PReDoc now? (y/N):${bold_off} "
					read inp
					if test $inp = "y" -o $inp = "Y" ; then
						install_predoc
					fi
					;;
		esac
	done

echo " "
