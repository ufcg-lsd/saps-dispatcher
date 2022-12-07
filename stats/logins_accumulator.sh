#!/bin/bash
cut -d',' -f2 logins_history.csv | awk 'END{printf "total_logins,"}{s+=$1}END{print s}'
