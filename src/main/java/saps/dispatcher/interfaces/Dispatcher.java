package saps.dispatcher.interfaces;

import java.util.List;
import java.util.Date;

public interface Dispatcher {

  List<String> createJobSubmission(
      String lowerLeftLatitude,
      String lowerLeftLongitude,
      String upperRightLatitude,
      String upperRightLongitude,
      Date initDate,
      Date endDate,
      String inputdownloadingPhaseTag,
      String preprocessingPhaseTag,
      String processingPhaseTag,
      int priority,
      String userEmail,
      String label)
      throws Exception;

  List<SapsUserJob> getAllJobs(JobState state, String search, Integer page, Integer size, String sortField,
      String sortOrder, boolean withoutTasks, boolean recoverOngoing, boolean recoverCompleted);

  Integer getJobsCount(JobState state, String search, boolean recoverOngoing, boolean recoverCompleted);

  List<SapsImage> getJobTasks(String jobId, ImageTaskState state, String search, Integer page,
      Integer size, String sortField, String sortOrder, boolean recoverOngoing, boolean recoverCompleted);
      
  SapsImage getTask(String taskId);
    
  Integer getCountTasks(String search, ImageTaskState state);

  SapsUser getUser(String email);

  void addUser(
      String email,
      String name,
      String password,
      boolean state,
      boolean notify,
      boolean adminRole);
    
  List<SapsImage> getProcessedTasks(
      String lowerLeftLatitude,
      String lowerLeftLongitude,
      String upperRightLatitude,
      String upperRightLongitude,
      Date initDate,
      Date endDate,
      String inputdownloadingPhaseTag,
      String preprocessingPhaseTag,
      String processingPhaseTag);
}
