today=`date +%F`
fname=saps-execution.log.$today

if [ -f $fname ]; then
	result= `grep -c "successfully authenticated" $fname`
	echo $today, $result, - >> ~/saps-dispatcher/stats/access_stats.csv
else
	result=0
	echo $today, $result, - >> ~/saps-dispatcher/stats/access_stats.csv
fi

