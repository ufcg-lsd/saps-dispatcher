RNAME=`dirname $0`
#cd DIRNAME ../

today=`date +%F`
fname=/home/ubuntu/saps-dispatcher/logs/saps-execution.log.$today

if [ -f $fname ]; then
        result=`grep -c "successfully authenticated" $fname`
        echo $today,$result                    
        echo $today,$result >> /home/ubuntu/saps-dispatcher/stats/logins_history.csv
else
        result=0
        echo $today,$result
        echo $today,$result >> /home/ubuntu/saps-dispatcher/stats/logins_history.csv
fi
