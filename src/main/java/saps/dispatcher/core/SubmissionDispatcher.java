/* (C)2020 */
package saps.dispatcher.core;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
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

  /**
   * It checks if the {@code SapsImage} is valid.
   * @param region is the location of the satellite data following the global
   * @param date is the date on which the satellite data was collected following
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

    List<String> tasksIds = new ArrayList<String>();
    List<Object[]> tasksData = new ArrayList<Object[]>();
    List<Object[]> tasksDataSync = Collections.synchronizedList(tasksData);
    String taskIdBase = inputdownloadingPhaseTag.substring(0, 3) + "_" + preprocessingPhaseTag + "_"
        + processingPhaseTag.replace("-", "") + "_";

    Thread producer = new Thread(() -> {
      while (cal.before(endCal)) {
        for (String region : regions) {
          Boolean isValidImage = validateLandsatImage(region, cal.getTime());
          if (isValidImage) {
            int startingYear = cal.get(Calendar.YEAR);
            List<String> datasets = DatasetUtil.getSatsInOperationByYear(startingYear);
            for (String dataset : datasets) {
              String date = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
              String taskId = taskIdBase + dataset + "_" + date + "_" + region;
              tasksDataSync.add(new Object[] { taskId, cal.getTime(), dataset, region });
              tasksIds.add(taskId);
            }
          }
          ;
          cal.add(Calendar.DAY_OF_YEAR, 1);
        }
      }
    });
    producer.start();

    Thread[] consumers = new Thread[32];
    for (int i = 0; i < consumers.length; i++) {
      consumers[i] = new Thread(() -> {
        while (producer.isAlive() || !tasksDataSync.isEmpty()) {
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
      });
      consumers[i].start();
    }

    try {
      producer.join();
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
          tasksIds,
          userEmail);

      for (int i = 0; i < consumers.length; i++) {
        consumers[i].join();
      }
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

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

  public List<SapsUserJob> getAllJobs(String state, String search, Integer page, Integer size, String sortField,
      String sortOrder, boolean withoutTasks) {
    return CatalogUtils.getUserJobs(catalog, search, page, size, sortField,
        sortOrder, "get jobs");
  }

  public Integer getJobsCount(String state) {
    return CatalogUtils.getUserJobsCount(catalog, state, "get amount of jobs");
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
