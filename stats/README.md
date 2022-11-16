# STATS 

This directory contains *three* .csv files. Each file does have info about SAPS "history", you can read more about these files below.

1. Login history

Briefly: how much people logged in SAPS by day.
The file *access_stats.csv* contains two columns: date, logins. Where date is the day and logins is how much logins we find in that day.

A row example: 2022-10-28, 3. That means: In 2022-10-28 SAPS had 3 logins.

2. Processing history

Tasks in SAPS got three main states: inputdownloading, preprocessing, processing.
The file *archived_overview_data.csv* contains four columns: task_id, inputdownloading, preprocessing, processing. Where task_id is a unique identifier of a task and the other columns indicates how much files has been used in order to perform the submission.

A row example:
aeiou-123-wxyz-456-defg, 81, 19, 22. That means: The task aeiou-123-wxyz-456-defg got: 81 steps in inputdownlaoding, 19 in preprocessing and 22 in processing.

3. General Info

The file *task-info-overview.csv* got the most complete info about SAPS submissions.
In this file you can find: How much valid and invalid images we got. In the valid ones, we can check where they stopped (in state): downloading, preprocessing, running, archived (failed), archived (success). And, we can see the size of downloaded data (in bytes) required to perform the task submission. Also, we can check the time spent to accomplish the submission pipeline.
