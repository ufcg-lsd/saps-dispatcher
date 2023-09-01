/* (C)2020 */
package saps.dispatcher.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import saps.catalog.core.Catalog;
import saps.catalog.core.jdbc.JDBCCatalog;
import saps.catalog.core.retry.CatalogUtils;
import saps.common.core.model.SapsImage;
import saps.common.core.model.SapsLandsatImage;
import saps.common.core.model.SapsUser;
import saps.common.core.model.enums.ImageTaskState;
import saps.common.utils.ExecutionScriptTag;
import saps.common.utils.ExecutionScriptTagUtil;
import saps.dispatcher.interfaces.Dispatcher;
import saps.dispatcher.utils.DatasetUtil;
import saps.dispatcher.utils.DigestUtil;
import saps.dispatcher.utils.RegionUtil;

public class SubmissionDispatcher implements Dispatcher {

  public static final String EXECUTION_TAGS_FILE_PATH_KEY = "EXECUTION_SCRIPT_TAGS_FILE_PATH";
  private final Catalog catalog;

  private static final Logger LOGGER = Logger.getLogger(SubmissionDispatcher.class);

  public SubmissionDispatcher(Catalog catalog) {
    this.catalog = catalog;
  }

  public SubmissionDispatcher(Properties properties) throws SQLException {
    this.catalog = new JDBCCatalog(properties);
  }

  public void addUser(
      String email,
      String name,
      String password,
      boolean state,
      boolean notify,
      boolean adminRole) {
    CatalogUtils.addNewUser(
        catalog, email, name, password, state, notify, adminRole, "add new user [" + email + "]");
  }

  public SapsUser getUser(String email) {
    return CatalogUtils.getUser(catalog, email, "get user [" + email + "] information");
  }

  public List<String> addTasks(
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
      throws Exception {

    List<String> taskIds = new LinkedList<String>();

    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(initDate);
    GregorianCalendar endCal = new GregorianCalendar();
    endCal.setTime(endDate);
    endCal.add(Calendar.DAY_OF_YEAR, 1);

    String tagsFilePath = System.getProperty(EXECUTION_TAGS_FILE_PATH_KEY);
    ExecutionScriptTag imageDockerInputdownloading = ExecutionScriptTagUtil.getExecutionScriptTag(
        tagsFilePath, inputdownloadingPhaseTag, ExecutionScriptTagUtil.INPUT_DOWNLOADER);
    ExecutionScriptTag imageDockerPreprocessing = ExecutionScriptTagUtil.getExecutionScriptTag(
        tagsFilePath, preprocessingPhaseTag, ExecutionScriptTagUtil.PRE_PROCESSING);
    ExecutionScriptTag imageDockerProcessing = ExecutionScriptTagUtil.getExecutionScriptTag(
        tagsFilePath, processingPhaseTag, ExecutionScriptTagUtil.PROCESSING);

    String digestInputdownloading = DigestUtil.getDigest(imageDockerInputdownloading);
    String digestPreprocessing = DigestUtil.getDigest(imageDockerPreprocessing);
    String digestProcessing = DigestUtil.getDigest(imageDockerProcessing);

    Set<String> regions = RegionUtil.regionsFromArea(
        lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);

    while (cal.before(endCal)) {
      int startingYear = cal.get(Calendar.YEAR);
      List<String> datasets = DatasetUtil.getSatsInOperationByYear(startingYear);

      for (String dataset : datasets) {
        LOGGER.debug("Adding new tasks with dataset " + dataset);

        for (String region : regions) {
          String taskId = UUID.randomUUID().toString();
          Boolean isValidImage = validateLandsatImage(region, cal.getTime());

          if (isValidImage) {
            SapsImage task =
                addTask(taskId, dataset, region, cal.getTime(),priority, userEmail, 
                inputdownloadingPhaseTag, digestInputdownloading, preprocessingPhaseTag,
                digestPreprocessing, processingPhaseTag, digestProcessing);

            if (task != null) {
              addTimestampTaskInCatalog(task, "updates task [" + taskId + "] timestamp");
              taskIds.add(taskId);
            }
          }
        }
      }
      cal.add(Calendar.DAY_OF_YEAR, 1);
    }
    return taskIds;
  }

  /**
   * It adds a new Task in {@code Catalog}.<br>
   *
   * @param taskId                   an unique identifier for the SAPS task.
   * @param dataset                  it is the type of data set associated with
   *                                 the new task to be created. Their
   *                                 values ​​can be:<br>
   *                                 -- landsat_5: indicates that the task is
   *                                 related to the LANDSAT 5 satellite
   *                                 (https://www.usgs.gov/land-resources/nli/landsat/landsat-5)<br>
   *                                 -- landsat_7: indicates that the task is
   *                                 related to the LANDSAT 7 satellite
   *                                 (https://www.usgs.gov/land-resources/nli/landsat/landsat-7)<br>
   *                                 -- landsat_8: indicates that the task is
   *                                 related with the LANDSAT 8 satellite data set
   *                                 (https://www.usgs.gov/land-resources/nli/landsat/landsat-8)<br>
   * @param region                   is the location of the satellite data
   *                                 following the global notation system for
   *                                 Landsat data (WRS:
   *                                 https://landsat.gsfc.nasa.gov/the-worldwide-reference-system),
   *                                 following
   *                                 the PPPRRR form, where PPP is a length-3 to
   *                                 the path number and RRR is a length-3 to the
   *                                 row number.
   * @param date                     is the date on which the satellite data was
   *                                 collected following the YYYY/MM/DD
   *                                 format.
   * @param priority                 is an integer in the [0, 31] range that
   *                                 indicates the priority of task
   *                                 processing.
   * @param userEmail                it is the email of the user that has
   *                                 submitted the task
   * @param inputdownloadingPhaseTag is the version of the algorithm that will be
   *                                 used in the task's
   *                                 inputdownloading step
   * @param digestInputdownloading   is the version of the algorithm that will be
   *                                 used in the task's
   *                                 preprocessing step
   * @param preprocessingPhaseTag    is the version of the algorithm that will be
   *                                 used in the task's
   *                                 processing step
   * @param digestPreprocessing      is the immutable identifier (digest) of the
   *                                 Docker image of the
   *                                 version defined in the inputdownloading step
   *                                 (inputdownloadingPhaseTag)
   * @param processingPhaseTag       is the immutable identifier (digest) of the
   *                                 Docker image of the
   *                                 version defined in the preprocessing step
   *                                 (preprocessingPhaseTag)
   * @param digestProcessing         is the immutable identifier (digest) of the
   *                                 Docker image of the version
   *                                 defined in the processing step
   *                                 (processingPhaseTag)
   * @return the new {@code SapsImage} created and added to this {@code Catalog}.
   */
  private SapsImage addTask(
      String taskId,
      String dataset,
      String region,
      Date date,
      int priority,
      String userEmail,
      String inputdownloadingPhaseTag,
      String digestInputdownloading,
      String preprocessingPhaseTag,
      String digestPreprocessing,
      String processingPhaseTag,
      String digestProcessing) {
    return CatalogUtils.addNewTask(
        catalog,
        taskId,
        dataset,
        region,
        date,
        priority,
        userEmail,
        inputdownloadingPhaseTag,
        digestInputdownloading,
        preprocessingPhaseTag,
        digestPreprocessing,
        processingPhaseTag,
        digestProcessing,
        "add new task [" + taskId + "]");
  }

  public List<SapsImage> getTasksByState(ImageTaskState state) throws SQLException {
    return CatalogUtils.getTasks(catalog, state);
  }

  private void addTimestampTaskInCatalog(SapsImage task, String message) {
    CatalogUtils.addTimestampTask(catalog, task);
  }

  /**
   * It checks if the {@code SapsImage} is valid.
   * 
   * @param region is the location of the satellite data following the global
   * @param date   is the date on which the satellite data was collected following
   * @return true if the {@code SapsImage} is valid, false otherwise.
   */
  private Boolean validateLandsatImage(String region, Date date) {
    SapsLandsatImage sapsLandsatImage = CatalogUtils.validateLandsatImage(
        catalog,
        region,
        date,
        "Validate Landsat Image " + date + " " + region);

    return sapsLandsatImage != null;
  }

  public List<SapsImage> getTasks(String search, Integer page, Integer size,
    String sortField, String sortOrder, ImageTaskState state) throws SQLException {
    
    if (state == ImageTaskState.CREATED || state == ImageTaskState.DOWNLOADING ||  state == ImageTaskState.DOWNLOADED 
    || state == ImageTaskState.PREPROCESSING || state == ImageTaskState.PREPROCESSED || state == ImageTaskState.READY
    || state == ImageTaskState.RUNNING ) {
        return CatalogUtils.getTasksOngoingWithPagination(catalog, search, page, size, sortField,
            sortOrder);
    } else if (state == ImageTaskState.FINISHED || state == ImageTaskState.ARCHIVING || state == ImageTaskState.ARCHIVED || state == ImageTaskState.FAILED) {
        return CatalogUtils.getTasksCompletedWithPagination(catalog, search, page, size, sortField,
            sortOrder);
    } else {
        return CatalogUtils.getTasks(catalog, state);
    }
  }

  public SapsImage getTask(String taskId) {
    return CatalogUtils.getTaskById(catalog, taskId, "gets task with id [" + taskId + "]");
  }

  public Integer getCountOngoingTasks(String search) {
    return CatalogUtils.getCountOngoingTasks(catalog, search, "get ongoing amount of tasks");
  }

  public Integer getCountCompletedTasks(String search) {
    return CatalogUtils.getCountCompletedTasks(catalog, search, "get completed amount of tasks");
  }

  public List<SapsImage> getAllTasks() {
    return CatalogUtils.getAllTasks(catalog, "get all tasks");
  }

  public List<SapsImage> getProcessedTasks(
      String lowerLeftLatitude,
      String lowerLeftLongitude,
      String upperRightLatitude,
      String upperRightLongitude,
      Date initDate,
      Date endDate,
      String inputdownloadingPhaseTag,
      String preprocessingPhaseTag,
      String processingPhaseTag) {

    List<SapsImage> filteredTasks = new ArrayList<>();
    Set<String> regions = RegionUtil.regionsFromArea(
        lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);

    for (String region : regions) {
      List<SapsImage> tasksInCurrentRegion = CatalogUtils.getProcessedTasks(
          catalog,
          region,
          initDate,
          endDate,
          inputdownloadingPhaseTag,
          preprocessingPhaseTag,
          processingPhaseTag,
          "gets all processed tasks with region ["
              + region
              + "], inputdownloading tag ["
              + inputdownloadingPhaseTag
              + "], preprocessing tag ["
              + preprocessingPhaseTag
              + "], processing tag ["
              + processingPhaseTag
              + "] beetwen "
              + initDate
              + " and "
              + endDate);
      filteredTasks.addAll(tasksInCurrentRegion);
    }
    return filteredTasks;
  }

}
