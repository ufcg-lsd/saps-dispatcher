#!/bin/bash
DIRNAME=`dirname $0`
cd $DIRNAME/..

today=`date +%F`
fname=./logs/saps-execution.log.$today

if [ -f $fname ]; then
    result=`grep -c "successfully authenticated" $fname`
    echo $today,$result >> ./stats/logins_history.csv
else
    result=0
    echo $today,$result >> ./stats/logins_history.csv
fi

