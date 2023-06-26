import sys

if __name__ == "__main__":
    #task_id,state,creation_time,updated_time,spent_time,total_size,valid_image,product_id,error_msg,last_phase
    header = sys.stdin.readline()

    valid_tasks = {}
    invalid_tasks = {}#because there is no satellite data to them
    for line in sys.stdin.readlines():
        try:
            task_id,state,creation_time,updated_time,spent_time,total_size,valid_image,product_id,error_msg,last_phase = line.split(",")
            if valid_image == "True":
                valid_tasks[task_id] = (state, int(total_size), last_phase, float(spent_time))
            else:
                invalid_tasks[task_id] = (state, last_phase)
        except Exception as e:
            print(e)

    print("valid tasks: " + str(len(valid_tasks)))
    print("invalid tasks: " + str(len(invalid_tasks)))

    total_size = 0
    for task in valid_tasks.values():
        task_size = task[1]
        total_size = total_size + task_size

    print("total_size valid (GB): " + str(total_size/1024/1024/1024))
