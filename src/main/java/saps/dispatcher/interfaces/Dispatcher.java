package saps.dispatcher.interfaces;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

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

  List<SapsImage> getTasks(String search, Integer page, Integer size,
    String sortField, String sortOrder, ImageTaskState state) throws SQLException;

  SapsImage getTask(String taskId);
    
  Integer getCountTasks(String search, ImageTaskState state);

  List<String> addTasks(
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
      String userEmail)
      throws Exception;

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
