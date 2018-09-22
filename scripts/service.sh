#!/bin/bash
# Annette platform
### BEGIN INIT INFO
# Provides:             annette-imc
# Required-Start:       $remote_fs $all
# Required-Stop:
# Default-Start:        2 3 4 5
# Default-Stop:         0 1 6
# Short-Description:    Annette platform
# Description:          Annette platform
### END INIT INFO
#
# Usage: control.sh {start|stop|status|restart}
#    port - requred for start and restart commands
#

# Script arguments (start, stop, restart or status)
COMMAND=$1

# ***********************************************
# *************  Set these variables  ***********

NAME=annette-akka
APP_DIR=/opt/annette/annette-akka-3.0.0


# ***********************************************
# ***********************************************

# Additional arguments to be passed to the Play application
APP_ARGS=" -J-Xms1048M -J-Xmx6192m -J-server"

# Path to the RUNNING_PID file containing process ID
PID_FILE=$APP_DIR/RUNNING_PID

# Helper functions
echoProgress()
{
	setColor 6
        printf "%-70s" "$1..."
	resetColor
	return 0
}

echoError()
{
	setColor 6
        printf "ERROR"
        if [ ! -z "$1" ]
        then
		resetColor
                printf " [$1]"
        fi
        printf "\n"
	resetColor
	return 0
}

echoOK()
{
	setColor 2
        printf "OK"
        if [ ! -z "$1" ]
        then
		resetColor
                printf " [$1]"
        fi
        printf "\n"
	resetColor
	return 0
}

checkResult()
{
        if [ "$1" -ne 0 ]
        then
                echoError "$2"
                exit 1
        fi
}

setColor()
{
        tput setaf $1 2>/dev/null
}

resetColor()
{
        tput sgr0 2>/dev/null
}

# Checks if RUNNING_PID file exists and whether the process is really running.
checkPidFile()
{
	if [ -f $PID_FILE ]
	then
		if ps -p `cat $PID_FILE` > /dev/null
		then
			# The file exists and the process is running
			return 1
		else
			# The file exitsts, but the process is dead
			return 2
		fi
	fi

	# The file doesn't exist
	return 0
}

# Gently kill the given process
kill_softly()
{
	SAFE_CHECK=`ps $@ | grep $APP_DIR`
	if [ -z "$SAFE_CHECK" ]
	then
		# Process ID doesn't belong to expected application! Don't kill it!
		return 1
	else
		# Send termination signals one by one
		for sig in TERM HUP INT QUIT PIPE KILL; do
			if ! kill -$sig "$@" > /dev/null 2>&1 ;
			then
				break
			fi
			sleep 2
		done
	fi
}

# Get process ID from RUNNING_PID file and print it
printPid()
{
	PID=`cat $PID_FILE`
	printf "PID=$PID"
}

# Check input arguments
checkArgs()
{
	# Check command
	case "$COMMAND" in
		start | stop | restart | status) ;;
		*)
			echoError "Unknown command"
			return 1
		;;
	esac

	# Check application name
	if [ -z "$NAME" ]
	then
		echoError "Application name not set!"
		return 1
	fi

	# Check application directory
	if [ -z "$APP_DIR" ]
	then
		echoError "Application installation directory not set!"
		return 1
	fi


}

checkAppStarted()
{
	# Wait a bit
	sleep 3

	# Check if RUNNING_PID file exists and if process is really running
	checkPidFile
	if [ $? != 1 ]
	then
		echoError
		cat $TMP_LOG 1>&2
		exit 1
	fi

#	local HTTP_RESPONSE_CODE

	# Issue HTTP GET request using wget to check if the app is really started. Of course this
	# command assumes that your server supports GET for the root URL.
#	HTTP_RESPONSE_CODE=`wget -SO- "http://localhost:$PORT/annette" 2>&1 | grep "HTTP/" | awk '{print $2}'`

	# The same functionality but using curl. For your convenience.
	#HTTP_RESPONSE_CODE=`curl --connect-timeout 20 --retry 3 -o /dev/null --silent --write-out "%{http_code}" http://localhost:$PORT/`

#	checkResult $? "no response from server, timeout"

#	if [ "$HTTP_RESPONSE_CODE" != "200" ]
#	then
#		echoError "HTTP GET / = $HTTP_RESPONSE_CODE"
#		exit 1
#	fi
}

# Check input arguments
checkArgs
if [ $? != 0 ]
then
	echo "Usage: $0 {start|stop|status|restart}"
	exit 1
fi

case "${COMMAND}" in
	start)
		echoProgress "Starting $NAME "

		checkPidFile
		case $? in
			1)	echoOK "$(printPid) already started"
				exit ;;
			2)	# Delete the RUNNING_PID FILE
				rm $PID_FILE ;;
		esac

		SCRIPT_TO_RUN=$APP_DIR/bin/$NAME
		if [ ! -f $SCRIPT_TO_RUN ]
		then
			echoError "Start script doesn't exist!"
			exit 1
		fi

		# * * * Run the application * * *
		TMP_LOG=`mktemp`

		cd $APP_DIR

		PID=`$SCRIPT_TO_RUN $APP_ARGS > /dev/null 2>$TMP_LOG & echo $!`

		# Check if successfully started
#		if [ $? != 0 ]
#		then
#			echoError
#			exit 1
#		else
			echo $PID > $PID_FILE
#			checkAppStarted
			echoOK "PID=$PID"
#		fi
	;;
	status)
		echoProgress "Checking $NAME "
		checkPidFile
		case $? in
			0)	echoOK "not running" ;;
			1)	echoOK "$(printPid) running" ;;
			2)	echoError "process dead but RUNNING_PID file exists" ;;
		esac
	;;
	stop)
		echoProgress "Stopping $NAME"
		checkPidFile
		case $? in
			0)	echoOK "wasn't running" ;;
			1)	PRINTED_PID=$(printPid)
				kill_softly `cat $PID_FILE`
				if [ $? != 0 ]
				then
					echoError "$PRINTED_PID doesn't belong to $NAME! Human intervention is required."
					exit 1
				else
					rm $PID_FILE
					echoOK "$PRINTED_PID stopped"
				fi ;;
			2)	echoError "RUNNING_PID exists but process is already dead" ;;
		esac
	;;

	restart)
		$0 stop $NAME
		if [ $? == 0 ]
		then
			$0 start $NAME
			if [ $? == 0 ]
			then
				# Success
				exit
			fi
		fi
		exit 1
	;;
esac
