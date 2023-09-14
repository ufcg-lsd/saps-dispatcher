/* (C)2020 */
package saps.dispatcher.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
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
import saps.common.core.model.SapsUserJob;
import saps.common.core.model.enums.ImageTaskState;
import saps.common.core.model.enums.JobState;
import saps.common.utils.ExecutionScriptTag;
import saps.common.utils.ExecutionScriptTagUtil;
import saps.dispatcher.utils.DatasetUtil;
import saps.dispatcher.utils.DigestUtil;
import saps.dispatcher.utils.RegionUtil;

public class SubmissionDispatcher {

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
  private void addTask(
      List<Object[]> tasksDataSync,
      int priority,
      String userEmail,
      String inputdownloadingPhaseTag,
      String digestInputdownloading,
      String preprocessingPhaseTag,
      String digestPreprocessing,
      String processingPhaseTag,
      String digestProcessing) {

    try {
      Object[] tData = tasksDataSync.remove(0);
      if (tData != null) {
        String taskId = (String) tData[0];
        Date day = (Date) tData[1];
        String dataset = (String) tData[2];
        String region = (String) tData[3];

        LOGGER.debug("Adding new tasks with dataset " + dataset + " and date " + day + " and region " + region);
        CatalogUtils.addNewTask(
            catalog,
            taskId,
            dataset,
            region,
            day,
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
    } catch (Exception e) {
      LOGGER.error("Error while adding new task", e);
    }
  }

  /**
   * It adds a new Job in {@code Catalog}.<br>
   * 
   * @param jobId               an unique identifier for the SAPS job.
   * @param lowerLeftLatitude   is the latitude of the lower left corner of the
   *                            region of interest
   * @param lowerLeftLongitude  is the longitude of the lower left corner of the
   *                            region of interest
   * @param upperRightLatitude  is the latitude of the upper right corner of the
   *                            region of interest
   * @param upperRightLongitude is the longitude of the upper right corner of the
   *                            region of interest
   * @param startDate           is the start date of the region of interest
   * @param endDate             is the end date of the region of interest
   * @param priority            is an integer in the [0, 31] range that indicates
   *                            the priority of job processing.
   * @param jobLabel            is the label of the job
   * @param tasksIds            is the list of tasks ids
   * @param userEmail           it is the email of the user that has submitted the
   *                            job
   */
  private void addUserJob(
      String jobId,
      String lowerLeftLatitude,
      String lowerLeftLongitude,
      String upperRightLatitude,
      String upperRightLongitude,
      Date startDate,
      Date endDate,
      int priority,
      String jobLabel,
      List<String> tasksIds,
      String userEmail) {
    CatalogUtils.addNewUserJob(
        catalog,
        jobId,
        lowerLeftLatitude,
        lowerLeftLongitude,
        upperRightLatitude,
        upperRightLongitude,
        userEmail,
        jobLabel,
        startDate,
        endDate,
        priority,
        tasksIds,
        "add new job [" + jobLabel + "]");
  }

  private void insertJobTask(
      String taskId,
      String jobId) {
    CatalogUtils.insertJobTask(
        catalog,
        taskId,
        jobId,
        "insert task [" + taskId + "]" + " into job [" + jobId + "]");
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

  public List<String> createJobSubmission(
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
      throws Exception {
    
    List<String> taskIds = new ArrayList<String>();
    String jobId = UUID.randomUUID().toString();
    addUserJob(
        jobId,
        lowerLeftLatitude,
        lowerLeftLongitude,
        upperRightLatitude,
        upperRightLongitude,
        initDate,
        endDate,
        priority,
        label,
        taskIds,
        userEmail
        );

    LOGGER.info("Job [" + jobId + "] was created");
    
    taskIds = createJobTasks(
        jobId,
        lowerLeftLatitude,
        lowerLeftLongitude,
        upperRightLatitude,
        upperRightLongitude,
        initDate,
        endDate,
        inputdownloadingPhaseTag,
        preprocessingPhaseTag,
        processingPhaseTag,
        priority,
        userEmail,
        label);
    
    LOGGER.debug("created tasks");
    return taskIds;
  }

  private List<String> createJobTasks(
      String jobId,
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
      throws Exception {
        
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(initDate);
    GregorianCalendar endCal = new GregorianCalendar();
    endCal.setTime(endDate);
    endCal.add(Calendar.DAY_OF_YEAR, 1);

    String tagsFilePath = System.getProperty(EXECUTION_TAGS_FILE_PATH_KEY);
    ExecutionScriptTag imageDockerInputdownloading = ExecutionScriptTagUtil.getExecutionScriptTag(
        tagsFilePath, inputdownloadingPhaseTag, ExecutionScriptTagUtil.INPUT_DOWNLOADER);
        LOGGER.info("inputd downloading tag: " + imageDockerInputdownloading);
    ExecutionScriptTag imageDockerPreprocessing = ExecutionScriptTagUtil.getExecutionScriptTag(
        tagsFilePath, preprocessingPhaseTag, ExecutionScriptTagUtil.PRE_PROCESSING);
        LOGGER.info("processing tag: " + imageDockerPreprocessing);
    ExecutionScriptTag imageDockerProcessing = ExecutionScriptTagUtil.getExecutionScriptTag(
        tagsFilePath, processingPhaseTag, ExecutionScriptTagUtil.PROCESSING);

    String digestInputdownloading = DigestUtil.getDigest(imageDockerInputdownloading);
    String digestPreprocessing = DigestUtil.getDigest(imageDockerPreprocessing);
    String digestProcessing = DigestUtil.getDigest(imageDockerProcessing);

    Set<String> regions = RegionUtil.regionsFromArea(
        lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);

    List<String> tasksIds = new ArrayList<String>();
    List<Object[]> tasksData = new ArrayList<Object[]>();
    List<Object[]> tasksDataSync = Collections.synchronizedList(tasksData);
        
    while (cal.before(endCal)) {
      for (String region : regions) {
        Boolean isValidImage = validateLandsatImage(region, cal.getTime());
        if (isValidImage) {
          int startingYear = cal.get(Calendar.YEAR);
          List<String> datasets = DatasetUtil.getSatsInOperationByYear(startingYear);
          for (String dataset : datasets) {
            String taskId = UUID.randomUUID().toString();
            tasksDataSync.add(new Object[] { taskId, cal.getTime(), dataset, region });
            LOGGER.debug("inserting task [" + taskId + "] into [" + jobId  + "]");
            insertJobTask(taskId, jobId);
            LOGGER.debug("task inserted");
            tasksIds.add(taskId);
          }
        };
        cal.add(Calendar.DAY_OF_YEAR, 1);
      }
    }
   
    while (!tasksDataSync.isEmpty()) {
        synchronized (tasksDataSync) {
        if (!tasksDataSync.isEmpty()) {
          addTask(
              tasksDataSync,
              priority,
              userEmail,
              inputdownloadingPhaseTag,
              digestInputdownloading,
              preprocessingPhaseTag,
              digestPreprocessing,
              processingPhaseTag,
              digestProcessing);
          }
        }
      }

    LOGGER.info("All tasks were created");
    CatalogUtils.updateUserJob(catalog, jobId, JobState.CREATED, "update job state to CREATED");
    return tasksIds;
  }

  public SapsImage getTask(String taskId) {
    return CatalogUtils.getTaskById(catalog, taskId, "gets task with id [" + taskId + "]");
  }

  public List<SapsImage> getTasksByState(ImageTaskState state) throws SQLException {
    return CatalogUtils.getTasks(catalog, state);
  }

  public List<SapsImage> getAllTasks() {
    return CatalogUtils.getAllTasks(catalog, "get all tasks");
  }

  public List<SapsUserJob> getAllJobs(JobState state, String search, Integer page, Integer size, String sortField,
      String sortOrder, boolean withoutTasks, boolean recoverOngoing, boolean recoverCompleted) {
    return CatalogUtils.getUserJobs(catalog, state, search, page, size, sortField, sortOrder, withoutTasks,
        recoverOngoing, recoverCompleted, "get jobs");
  }

  public Integer getJobsCount(JobState state, String search, boolean recoverOngoing, boolean recoverCompleted) {
    return CatalogUtils.getUserJobsCount(catalog, state, search, recoverOngoing, recoverCompleted, "get amount of jobs");
  }

  public List<SapsImage> getJobTasks(String jobId, ImageTaskState state, String search, Integer page,
      Integer size, String sortField, String sortOrder, boolean recoverOngoing, boolean recoverCompleted) {
    return CatalogUtils.getUserJobTasks(catalog, jobId, state, search, page, size, sortField, sortOrder,
        recoverOngoing, recoverCompleted, "get job tasks");
  }

  public Integer getJobTasksCount(String jobId, ImageTaskState state, String search, boolean recoverOngoing, boolean recoverCompleted) {
    return CatalogUtils.getUserJobTasksCount(catalog, jobId, state, search, recoverOngoing, recoverCompleted, "get amount of tasks");
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


