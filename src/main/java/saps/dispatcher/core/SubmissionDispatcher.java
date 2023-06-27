/* (C)2020 */
package saps.dispatcher.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.print.attribute.standard.JobState;

import org.apache.log4j.Logger;

import saps.dispatcher.interfaces.*;

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

  /**
   * It adds new User in {@code Catalog}.
   *
   * @param email     user email used for authentication on the SAPS platform
   * @param name      user name on the SAPS platform
   * @param password  user password used for authentication on the SAPS platform
   * @param notify    informs the user about their tasks by email.<br>
   * @param state     informs if the user is able to authenticate on the SAPS
   *                  platform (it for default
   *                  is false)
   * @param adminRole administrative role: informs if the user is an administrator
   *                  of the SAPS
   *                  platform (it for default is false)
   */
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

  /**
   * It gets {@code SapsUser} in {@code Catalog}.
   *
   * @return an {@code SapsUser} with equal email
   */
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

  /**
   * It gets information about a new processing submission and extract N
   * {@code SapsImage} for adds
   * them in {@code Catalog}.
   *
   * @param lowerLeftLatitude        is a geographic coordinate plus the lower
   *                                 left defined in the sphere
   *                                 which is the angle between the plane of the
   *                                 equator and the normal to the reference
   *                                 surface
   *                                 indicating the vertex of the polygon formed
   *                                 together with the information
   *                                 lowerLeftLongitude, upperRightLatitude and
   *                                 upperRightLongitude.
   * @param lowerLeftLongitude       is a geographic coordinate plus the lower
   *                                 left defined in the sphere
   *                                 measured in degrees, from 0 to 180 towards
   *                                 east or west, from the Greenwich Meridian
   *                                 indicating the vertex of the polygon formed
   *                                 together with the information
   *                                 lowerLeftLatitude, upperRightLatitude and
   *                                 upperRightLongitude.
   * @param upperRightLatitude       is a geographic coordinate plus the upper
   *                                 right defined in the sphere
   *                                 which is the angle between the plane of the
   *                                 equator and the normal to the reference
   *                                 surface
   *                                 indicating the vertex of the polygon formed
   *                                 together with the information
   *                                 lowerLeftLatitude, lowerLeftLongitude and
   *                                 upperRightLongitude.
   * @param upperRightLongitude      is a geographic coordinate plus the upper
   *                                 right defined in the
   *                                 sphere measured in degrees, from 0 to 180
   *                                 towards east or west, from the Greenwich
   *                                 Meridian
   *                                 indicating the vertex of the polygon formed
   *                                 together with the information
   *                                 lowerLeftLatitude, lowerLeftLongitude and
   *                                 upperRightLatitude.
   * @param initDate                 it is the starting date (according to the
   *                                 Gregorian calendar) of the interval
   *                                 in which the satellite data collection date
   *                                 must belong. If it belongs, a SAPS task will
   *                                 be
   *                                 created to process the satellite data.
   * @param endDate                  It is the end date (according to the
   *                                 Gregorian calendar) of the interval in
   *                                 which the satellite data collection date must
   *                                 belong. If this belongs, a SAPS task will be
   *                                 created to process the satellite data.
   * @param inputdownloadingPhaseTag is the version of the algorithm that will be
   *                                 used in the task's
   *                                 inputdownloading step.
   * @param preprocessingPhaseTag    is the version of the algorithm that will be
   *                                 used in the task's
   *                                 preprocessing step.
   * @param processingPhaseTag       is the version of the algorithm that will be
   *                                 used in the task's
   *                                 processing step.
   * @param priority                 it is an integer in the range 0 to 31 that
   *                                 indicates how priority the task
   *                                 processing is.
   * @param userEmail                it is the email of the task owner (this
   *                                 information is obtained automatically
   *                                 by the authenticated user on the platform).
   */
  public List<String> createJobTasks(
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
    
              SapsImage task = addTask(
                  taskId,
                  dataset,
                  region,
                  cal.getTime(),
                  priority,
                  userEmail,
                  inputdownloadingPhaseTag,
                  digestInputdownloading,
                  preprocessingPhaseTag,
                  digestPreprocessing,
                  processingPhaseTag,
                  digestProcessing);
              addTimestampTaskInCatalog(task, "updates task [" + taskId + "] timestamp");
              insertJobTask(taskId, jobId);
              taskIds.add(taskId);
            }
          }
          cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return taskIds;
  }

  private void addTimestampTaskInCatalog(SapsImage task, String string) {
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
    //we should use thread here 
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


  public List<SapsImage> getTasks(String search, Integer page, Integer size,
    String sortField, String sortOrder, ImageTaskState state) throws SQLException {
    
    if (state == ImageTaskState.ONGOING) {
        return CatalogUtils.getTasksOngoingWithPagination(catalog, search, page, size, sortField,
            sortOrder, "get paginated ongoing tasks");
    } else if (state == ImageTaskState.COMPLETED) {
        return CatalogUtils.getTasksCompletedWithPagination(catalog, search, page, size, sortField,
            sortOrder, "get paginated completed tasks");
    } else {
        return CatalogUtils.getTasks(catalog, state);
    }
  }

  public Integer getCountTasks(String search, ImageTaskState state) {
    if (state == ImageTaskState.ONGOING) {
        return CatalogUtils.getCountOngoingTasks(catalog, search, "get ongoing amount of tasks");
    } else if (state == ImageTaskState.COMPLETED) {
        return CatalogUtils.getCountCompletedTasks(catalog, search, "get completed amount of tasks");
    }
    return null;
  }

  public SapsImage getTask(String taskId) {
    return CatalogUtils.getTaskById(catalog, taskId, "gets task with id [" + taskId + "]");
  }

  /**
   * This function get all saps user job in Catalog.
   * 
   * @param state              state of jobs
   * @param search             search string
   * @param page               page number
   * @param size               page size
   * @param sortField          sort field
   * @param sortOrder          sort order
   * @param withoutTasks       without tasks
   * @param recoverOngoing     if true, only ongoing jobs will be recovered
   * @param recoverCompleted   if true, only completed jobs will be recovered
   * @return list of jobs
   */
  public List<SapsUserJob> getAllJobs(JobState state, String search, Integer page, Integer size, String sortField,
      String sortOrder, boolean withoutTasks, boolean recoverOngoing, boolean recoverCompleted) {
    return CatalogUtils.getUserJobs(catalog, state, search, page, size, sortField, sortOrder, withoutTasks,
        recoverOngoing, recoverCompleted, "get jobs");
  }

  /**
   * This function get tha amount of all jobs in Catalog.
   * 
   * @param state              state of jobs
   * @param search             search string
   * @param recoverOngoing     if true, only ongoing jobs will be recovered
   * @param recoverCompleted   if true, only completed jobs will be recovered
   * @return amount of all jobs
   */
  public Integer getJobsCount(JobState state, String search, boolean recoverOngoing, boolean recoverCompleted) {
    return CatalogUtils.getUserJobsCount(catalog, state, search, recoverOngoing, recoverCompleted, "get amount of jobs");
  }


  /**
   * It gets processed {@code SapsImage} in {@code Catalog} by filtering for
   * parameters.
   *
   * @param lowerLeftLatitude        is a geographic coordinate plus the lower
   *                                 left defined in the sphere
   *                                 which is the angle between the plane of the
   *                                 equator and the normal to the reference
   *                                 surface
   *                                 indicating the vertex of the polygon formed
   *                                 together with the information
   *                                 lowerLeftLongitude, upperRightLatitude and
   *                                 upperRightLongitude.
   * @param lowerLeftLongitude       is a geographic coordinate plus the lower
   *                                 left defined in the sphere
   *                                 measured in degrees, from 0 to 180 towards
   *                                 east or west, from the Greenwich Meridian
   *                                 indicating the vertex of the polygon formed
   *                                 together with the information
   *                                 lowerLeftLatitude, upperRightLatitude and
   *                                 upperRightLongitude.
   * @param upperRightLatitude       is a geographic coordinate plus the upper
   *                                 right defined in the sphere
   *                                 which is the angle between the plane of the
   *                                 equator and the normal to the reference
   *                                 surface
   *                                 indicating the vertex of the polygon formed
   *                                 together with the information
   *                                 lowerLeftLatitude, lowerLeftLongitude and
   *                                 upperRightLongitude.
   * @param upperRightLongitude      is a geographic coordinate plus the upper
   *                                 right defined in the
   *                                 sphere measured in degrees, from 0 to 180
   *                                 towards east or west, from the Greenwich
   *                                 Meridian
   *                                 indicating the vertex of the polygon formed
   *                                 together with the information
   *                                 lowerLeftLatitude, lowerLeftLongitude and
   *                                 upperRightLatitude.
   * @param initDate                 it is the starting date (according to the
   *                                 Gregorian calendar) of the interval
   *                                 in which the satellite data collection date
   *                                 must belong. If it belongs, a SAPS task will
   *                                 be
   *                                 created to process the satellite data.
   * @param endDate                  It is the end date (according to the
   *                                 Gregorian calendar) of the interval in
   *                                 which the satellite data collection date must
   *                                 belong. If this belongs, a SAPS task will be
   *                                 created to process the satellite data.
   * @param inputdownloadingPhaseTag is the version of the algorithm that will be
   *                                 used in the task's
   *                                 inputdownloading step.
   * @param preprocessingPhaseTag    is the version of the algorithm that will be
   *                                 used in the task's
   *                                 preprocessing step.
   * @param processingPhaseTag       is the version of the algorithm that will be
   *                                 used in the task's
   *                                 processing step.
   */
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
