#!/bin/sh

#############################################################################
# 
# Library Locator
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

# check for additional librarys
#--------------------------------------------------------------------

# things to:
# - add directories the user selects to `header_path' and `library_path'

ac_n=-n

header_path=" \
    /usr/X11/include          \
    /usr/X11R6/include        \
    /usr/X11R5/include        \
    /usr/X11R4/include        \
                              \
    /usr/include/X11          \
    /usr/include/X11R6        \
    /usr/include/X11R5        \
    /usr/include/X11R4        \
                              \
    /usr/local/X11/include    \
    /usr/local/X11R6/include  \
    /usr/local/X11R5/include  \
    /usr/local/X11R4/include  \
                              \
    /usr/local/include/X11    \
    /usr/local/include/X11R6  \
    /usr/local/include/X11R5  \
    /usr/local/include/X11R4  \
                              \
    /usr/X386/include         \
    /usr/x386/include         \
    /usr/XFree86/include/X11  \
                              \
    /usr/include              \
    /usr/local/include        \
    /usr/unsupported/include  \
    /usr/athena/include       \
    /usr/local/x11r5/include  \
    /usr/lpp/Xamples/include  \
                              \
    /usr/openwin/include      \
    /usr/openwin/share/include \
    /opt/include"

if test -z $LD_LIBRARY_PATH ; then
	library_path=""
else
	library_path=`echo $LD_LIBRARY_PATH | sed "s/:/  /g"`
fi

library_path="\
    /usr/X11/lib          \
    /usr/X11R6/lib        \
    /usr/X11R5/lib        \
    /usr/X11R4/lib        \
                          \
    /usr/lib/X11          \
    /usr/lib/X11R6        \
    /usr/lib/X11R5        \
    /usr/lib/X11R4        \
                          \
    /usr/local/X11/lib    \
    /usr/local/X11R6/lib  \
    /usr/local/X11R5/lib  \
    /usr/local/X11R4/lib  \
                          \
    /usr/local/lib/X11    \
    /usr/local/lib/X11R6  \
    /usr/local/lib/X11R5  \
    /usr/local/lib/X11R4  \
                          \
    /usr/X386/lib         \
    /usr/x386/lib         \
    /usr/XFree86/lib/X11  \
                          \
    /usr/lib              \
    /usr/local/lib        \
    /usr/unsupported/lib  \
    /usr/athena/lib       \
    /usr/local/x11r5/lib  \
    /usr/lpp/Xamples/lib  \
    /lib/usr/lib/X11	  	\
                          \
    /usr/openwin/lib      \
    /usr/openwin/share/lib\
    /opt/lib\
    ${library_path}"

check_library() {
	if test $4 -eq 1 ; then
		echo $ac_n "Do you want support for $3? (y/N)"": $ac_c"
		read tc_in
		if test -z $tc_in ; then
			return 1
		fi
		if test $tc_in != y ; then
			if test $tc_in != Y ; then
				return 1
			fi
		fi
	fi

  library_file=$2;
  header_file=$1
  include_dir=""
  library_dir=""

	echo $ac_n "checking for lib$2.[a|so]""... $ac_c"
  
	# 1st try to find a shared library
	#---------------------------------
  for tc_dir in `echo "$library_path"` ; do
    tc_libs=`(ls $tc_dir/lib${library_file}.so*) 2> /dev/null`
    if test $? -eq 0 ; then
    	for name in `echo $tc_libs` ; do
    		tc_lib=$name
    	done
    	library_dir=$tc_dir
    	break;
    fi
    let i=$i+1
  done

  # 2nd: when there's no shared library try to find a static one
  #-------------------------------------------------------------
	if test -z $library_dir ; then
	  for tc_dir in `echo "$library_path"` ; do
	    if test -r "$tc_dir/lib${library_file}.a" ; then
	    	tc_lib="$tc_dir/lib${library_file}.a"
	    	library_dir=$tc_dir
	      break
	    fi
	    let i=$i+1
	  done
	fi

	if test -z $library_dir ; then
    echo "no"
    return 0
	else
		echo "yes (${tc_lib})"
	fi

	# search for header file

  echo $ac_n "checking for $1""... $ac_c"

  for tc_hdir in `echo "$header_path"` ; do
    if test -r "$tc_hdir/$header_file" ; then
    	include_dir=$tc_hdir
      break
    fi
    let i=$i+1
  done

  if test -z $include_dir ; then
    tc_res=`locate "/$header_file" 2> /dev/null`
    if test $? -eq 0 ; then
      echo "perhaps"
      echo ""
cat > dummy.output << EOF
The file ${bold_on}${header_file}${bold_off} needed to \
support the "${3}" in library ${bold_on}${tc_lib}${bold_off} was found \
outside a standard path.
EOF
			fold -s dummy.output 2> /dev/null
			if test $? -ne 0 ; then
			  cat dummy.output
			fi
			rm -f dummy.output
			echo ""
      i=0
      for name in $tc_res ; do
        let i=$i+1
        echo "  [$i] $name"
      done
      echo ""
      echo "  [0] Don't use this library"
      echo ""
      echo 'Which one shall i use?'
      echo ""
			while true ; do
	      echo $ac_n "Enter a number: "
	      read tc_in
	      if test ! -z $tc_in ; then
	      	if test $tc_in = "0" ; then
	      		return 1
	      	fi
		      i=0
		      for name in $tc_res ; do
		        let i=$i+1
		      	if test $tc_in = $i ; then
			      	include_dir=`echo $name | sed "s%/$header_file%%"`
			      	echo "Using directory ${bold_on}${include_dir}${bold_off}."
			      fi
		      done
		    fi
	      if test ! -z $include_dir ; then
	      	break;
	      else
	      	echo "Please try again, it's not that difficult. ;)"
	      fi
      done
    else
      echo "no"
    fi
  else
    echo "yes"
  fi
  return 0
}

tcc_clear() {
	tcc_hdr=""
	tcc_lib=""
	tcc_ldr=""
	tcc_def=""
}

tcc_hdr_add() {
	if test "/usr/include" = $1 ; then
		return
	fi

	for name in ${tcc_hdr} ; do
		if test $name = $1 ; then
			return
		fi
	done
	tcc_hdr="${tcc_hdr} $1"
}

tcc_ldr_add() {
	if test "/usr/lib" = $1 ; then
		return
	fi
	for name in ${tcc_ldr} ; do
		if test $name = $1 ; then
			return
		fi
	done
	tcc_ldr="${tcc_ldr} $1"
}

tcc_lib_add() {
	for name in ${tcc_lib} ; do
		if test $name = $1 ; then
			return
		fi
	done
	tcc_lib="${tcc_lib} $1"
}

tcc_def_add() {
	for name in ${tcc_def} ; do
		if test $name = $1 ; then
			return
		fi
	done
	tcc_def="${tcc_def} $1"
}

tcc_write_file() {
cat > ${tcc_output} << EOF
#############################################################################
#
# PReDoc global makefile definitions
# generated automatically
#
#############################################################################

EOF

line="LIBS += "
for name in ${tcc_lib} ; do
	line="${line} -l${name}"
done
echo $line >> ${tcc_output}

line="INCDIRS += "
for name in ${tcc_hdr} ; do
	line="${line} -I${name}"
done
echo $line >> ${tcc_output}

line="LIBDIRS += "
for name in ${tcc_ldr} ; do
	line="${line} -L${name}"
done
echo $line >> ${tcc_output}

line="DEFINES += "
for name in ${tcc_def} ; do
	line="${line} -D${name}"
done
echo $line >> ${tcc_output}
}

#----------------------------------------------------------------------------

tcc_clear

tc_check_name="Portable Network Graphics (PNG)"
check_library png.h png "${tc_check_name}" 0
if test $? -eq 0 ; then
	tc_lib2=${library_file}
	tc_hd2=$include_dir
	tc_ld2=$library_dir
	check_library zlib.h z "${tc_check_name}" 0
	if test $? -eq 0 ; then
		echo "enabling ${bold_on}${tc_check_name}${bold_off}"
		tcc_lib_add ${tc_lib2}
		tcc_lib_add ${library_file}
		tcc_hdr_add ${tc_hd2}
		tcc_hdr_add ${include_dir}
		tcc_ldr_add ${tc_ld2}
		tcc_ldr_add ${library_dir}
		tcc_def_add "HAVE_PNG"
	fi
fi

#tc_check_name="GIF Library"
#check_library "gif_lib.h" "gif" "${tc_check_name}" 0
#if test $? -eq 0 ; then
#	echo "enabling ${bold_on}${tc_check_name}${bold_off}"
#	tcc_lib_add ${library_file}
#	tcc_hdr_add ${include_dir}
#	tcc_ldr_add ${library_dir}
#	tcc_def_add "HAVE_GIFLIB"
#fi

tc_check_name="POSIX Threads"
check_library pthread.h pthread "${tc_check_name}" 0
if test $? -eq 0 ; then
	echo "enabling ${bold_on}${tc_check_name}${bold_off}"
	tcc_lib_add ${library_file}
	tcc_hdr_add ${include_dir}
	tcc_ldr_add ${library_dir}
	tcc_def_add "HAVE_PTHREADS"
	tcc_def_add "_REENTRANT"
fi

tcc_write_file
