#!/bin/bash
downloaded=`cut -d',' -f2 archived_overview_data.csv | awk '{s+=$1}END{print s}'`
preprocessed=`cut -d',' -f3 archived_overview_data.csv | awk '{s+=$1}END{print s}'`
processed=`cut -d',' -f4 archived_overview_data.csv | awk '{s+=$1}END{print s}'`

echo "downloaded",$downloaded
echo "preprocessed",$preprocessed
echo "processed",$processed

echo "total_archived",`echo ${downloaded} + ${preprocessed} + ${processed} | bc`

