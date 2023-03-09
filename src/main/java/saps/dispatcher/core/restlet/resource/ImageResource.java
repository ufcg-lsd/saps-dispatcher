/* (C)2020 */
package saps.dispatcher.core.restlet.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import com.google.gson.Gson;

import saps.common.core.model.SapsImage;
import saps.common.core.model.SapsUserJob;
import saps.dispatcher.core.restlet.DatabaseApplication;

public class ImageResource extends BaseResource {

  private static final Logger LOGGER = Logger.getLogger(ImageResource.class);

  private static final String QUERY_KEY_JOB_ID = "jobId";
  private static final String QUERY_KEY_JOB_STATE = "jobState";
  private static final String QUERY_KEY_WITHOUT_TASKS = "withoutTasks";
  private static final String QUERY_KEY_ONLY_ONGOING_JOBS = "allOngoingJobs";
  private static final String QUERY_KEY_PAGINATION_PAGE = "page";
  private static final String QUERY_KEY_PAGINATION_SIZE = "size";
  private static final String QUERY_KEY_PAGINATION_SORT = "sort";
  private static final String QUERY_KEY_PAGINATION_SEARCH = "search";

  private static final String LOWER_LEFT = "lowerLeft";
  private static final String UPPER_RIGHT = "upperRight";
  private static final String PROCESSING_INIT_DATE = "initialDate";
  private static final String PROCESSING_FINAL_DATE = "finalDate";
  private static final String PROCESSING_INPUT_GATHERING_TAG = "inputGatheringTag";
  private static final String PROCESSING_INPUT_PREPROCESSING_TAG = "inputPreprocessingTag";
  private static final String PROCESSING_ALGORITHM_EXECUTION_TAG = "algorithmExecutionTag";
  private static final String PRIORITY = "priority";
  private static final String EMAIL = "email";
  private static final String LABEL = "label";

  private static final String ADD_IMAGES_MESSAGE_OK = "Tasks successfully added";
  private static final String ADD_JOB_MESSAGE_FAILURE = "Failed to add new jobs";

  private final Gson gson = new Gson();

  public ImageResource() {
    super();
  }

  //remove this
  private JSONObject getTaskById(String taskId) {
    LOGGER.info("Getting taskID" + taskId);

    SapsImage imageTask = ((DatabaseApplication) getApplication()).getTask(taskId);

    JSONArray tasksJSON = new JSONArray();
    JSONObject responseJSON = new JSONObject();
    try {
      tasksJSON.put(imageTask.toJSON());
      responseJSON.put("tasks", tasksJSON);
    } catch (JSONException e) {
      LOGGER.error("Error while creating JSON from image task " + imageTask, e);
    }

    return responseJSON;
  }

  //remove this
  private JSONObject getAllJTasks(String state, String search, Integer page, Integer size, JSONObject sortJSON) {
    LOGGER.info("Getting all tasks");

    Integer tasksCount = 0;
    JSONArray tasksJSON = new JSONArray();
    JSONObject responseJSON = new JSONObject();
    List<SapsImage> listOfTasks = new ArrayList<SapsImage>();
    DatabaseApplication app = (DatabaseApplication) getApplication();

    try {
      listOfTasks = app.getTasks();
      tasksCount = listOfTasks.size();

      for (SapsImage imageTask : listOfTasks) {
        tasksJSON.put(imageTask.toJSON());
      }

      responseJSON.put("tasks", tasksJSON); // Tasks with pagination
      responseJSON.put("allTasksCount", tasksCount); // Number of tasks without pagination
    } catch (JSONException e) {
      LOGGER.error("Error while creating JSON from image task ", e);
    }

    return responseJSON;
  }

  @SuppressWarnings("unchecked")
  @Get
  public Representation getAllJobs() throws Exception {
    Series<Header> series = (Series<Header>) getRequestAttributes().get("org.restlet.http.headers");
    String userEmail = series.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
    String userPass = series.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);
    String userEGI = series.getFirstValue(UserResource.REQUEST_ATTR_USER_EGI, true);
    String state = series.getFirstValue(QUERY_KEY_JOB_STATE, true);
    String search = series.getFirstValue(QUERY_KEY_PAGINATION_SEARCH, true);
    String jobId = series.getFirstValue(QUERY_KEY_JOB_ID, true);
    Integer page = Integer.parseInt(series.getFirstValue(QUERY_KEY_PAGINATION_PAGE, true));
    Integer size = Integer.parseInt(series.getFirstValue(QUERY_KEY_PAGINATION_SIZE, true));
    Boolean withoutTasks = Boolean.parseBoolean(series.getFirstValue(QUERY_KEY_WITHOUT_TASKS, true));
    Boolean allOngoingJobs = Boolean.parseBoolean(series.getFirstValue(QUERY_KEY_ONLY_ONGOING_JOBS, true));
    JSONObject sortJSON = new JSONObject(series.getFirstValue(QUERY_KEY_PAGINATION_SORT, true));

    if (!authenticateUser(userEmail, userPass, userEGI)) {
      throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);
    }

    String sortField = "";
    String sortOrder = "";
    JSONArray jobsJSON = new JSONArray();
    JSONObject responseJSON = new JSONObject();

    if (sortJSON.length() > 0) {
      sortField = sortJSON.keys().next().toString();
      sortOrder = sortJSON.get(sortField).toString();
    }

    if (jobId != null) {
      SapsImage jobTasks = application.getJobTasks(jobId, search, page, size, sortField, sortOrder);
      Integer tasksCount = application.getJobTasksCount(state, search, allOngoingJobs);
      jobsJSON.put(jobTasks.toJSON());
      responseJSON.put("tasks", jobsJSON);
      responseJSON.put("tasksCount", tasksCount); //edit this
    } else {
      Integer jobsCount = application.getJobsCount(state, search, allOngoingJobs);
      List<SapsUserJob> jobList = application.getAllJobs(state, search, page, size, sortField, sortOrder, withoutTasks, allOngoingJobs);
      for (SapsUserJob userJob : jobList) {
        jobsJSON.put(userJob.toJSON());
      }
      responseJSON.put("jobs", jobsJSON);
      responseJSON.put("jobsCount", jobsCount);  
    }
    
    return new StringRepresentation(responseJSON.toString(), MediaType.APPLICATION_JSON);
  }

  @Post
  public StringRepresentation createJobSubmission(Representation entity) {
    Form form = new Form(entity);

    String userEmail = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
    String userPass = form.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);
    String userEGI = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EGI, true);

    LOGGER.debug("POST with userEmail " + userEmail);
    if (!authenticateUser(userEmail, userPass, userEGI) || userEmail.equals("anonymous")) {
      throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);
    }

    String lowerLeftLatitude;
    String lowerLeftLongitude;
    String upperRightLatitude;
    String upperRightLongitude;
    try {
      lowerLeftLatitude = extractCoordinate(form, LOWER_LEFT, 0);
      lowerLeftLongitude = extractCoordinate(form, LOWER_LEFT, 1);
      upperRightLatitude = extractCoordinate(form, UPPER_RIGHT, 0);
      upperRightLongitude = extractCoordinate(form, UPPER_RIGHT, 1);
    } catch (Exception e) {
      LOGGER.error("Failed to parse coordinates of new processing.", e);
      throw new ResourceException(
          Status.CLIENT_ERROR_BAD_REQUEST, "All coordinates must be informed.");
    }

    Date initDate;
    Date endDate;
    try {
      initDate = extractDate(form, PROCESSING_INIT_DATE);
      endDate = extractDate(form, PROCESSING_FINAL_DATE);
    } catch (Exception e) {
      LOGGER.error("Failed to parse dates of new processing.", e);
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "All dates must be informed.");
    }

    String inputdownloadingPhaseTag = form.getFirstValue(PROCESSING_INPUT_GATHERING_TAG);
    if (inputdownloadingPhaseTag.isEmpty())
      throw new ResourceException(
          Status.CLIENT_ERROR_BAD_REQUEST, "Input Gathering must be informed.");
    String preprocessingPhaseTag = form.getFirstValue(PROCESSING_INPUT_PREPROCESSING_TAG);
    if (preprocessingPhaseTag.isEmpty())
      throw new ResourceException(
          Status.CLIENT_ERROR_BAD_REQUEST, "Input Preprocessing must be informed.");
    String processingPhaseTag = form.getFirstValue(PROCESSING_ALGORITHM_EXECUTION_TAG);
    if (processingPhaseTag.isEmpty())
      throw new ResourceException(
          Status.CLIENT_ERROR_BAD_REQUEST, "Algorithm Execution must be informed.");
    String priority = form.getFirstValue(PRIORITY);
    String email = form.getFirstValue(EMAIL);
    String label = form.getFirstValue(LABEL);

    String builder = "Creating new image process with configuration:\n"
        + "\tLower Left: "
        + lowerLeftLatitude
        + ", "
        + lowerLeftLongitude
        + "\n"
        + "\tUpper Right: "
        + upperRightLatitude
        + ", "
        + upperRightLongitude
        + "\n"
        + "\tInterval: "
        + initDate
        + " - "
        + endDate
        + "\n"
        + "\tInputdownloading tag: "
        + inputdownloadingPhaseTag
        + "\n"
        + "\tPreprocessing tag: "
        + preprocessingPhaseTag
        + "\n"
        + "\tProcessing tag: "
        + processingPhaseTag
        + "\n"
        + "\tPriority: "
        + priority
        + "\n"
        + "\tEmail: "
        + email
        + "\n"
        + "\tLabel: "
        + label;
    LOGGER.info(builder);

    try {
      List<String> taskIds = application.createJobSubmission(
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
          email,
          label);
      return new StringRepresentation(gson.toJson(taskIds), MediaType.APPLICATION_JSON);

    } catch (Exception e) {
      LOGGER.error("Error while add news tasks.", e);
      return new StringRepresentation(ADD_JOB_MESSAGE_FAILURE, MediaType.TEXT_PLAIN);
    }
  }
}
