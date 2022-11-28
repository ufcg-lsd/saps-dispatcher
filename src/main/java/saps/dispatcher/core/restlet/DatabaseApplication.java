/* (C)2020 */
package saps.dispatcher.core.restlet;

import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.service.ConnectorService;
import org.restlet.service.CorsService;
import saps.common.core.model.SapsImage;
import saps.common.core.model.SapsUser;
import saps.common.core.model.enums.ImageTaskState;
import saps.common.exceptions.SapsException;
import saps.common.utils.SapsPropertiesConstants;
import saps.common.utils.SapsPropertiesUtil;
import saps.dispatcher.core.SubmissionDispatcher;
import saps.dispatcher.core.restlet.resource.EmailResource;
import saps.dispatcher.core.restlet.resource.ImageResource;
import saps.dispatcher.core.restlet.resource.LinkResource;
import saps.dispatcher.core.restlet.resource.MainResource;
import saps.dispatcher.core.restlet.resource.RegionResource;
import saps.dispatcher.core.restlet.resource.TaskResource;
import saps.dispatcher.core.restlet.resource.UserResource;

// FIXME Delete any obvious java-doc
public class DatabaseApplication extends Application {
  private static final String DB_WEB_STATIC_ROOT = "./dbWebHtml/static";

  public static final Logger LOGGER = Logger.getLogger(DatabaseApplication.class);

  // FIXME Remove properties field and add new variables
  private Properties properties;
  private SubmissionDispatcher submissionDispatcher;
  private Component restletComponent;

  public DatabaseApplication(Properties properties) throws SapsException, SQLException {
    if (!checkProperties(properties))
      throw new SapsException(
          "Error on validate the file. Missing properties for start Database Application.");

    this.properties = properties;
    this.submissionDispatcher = new SubmissionDispatcher(properties);

    CorsService cors = new CorsService();
    cors.setAllowedOrigins(new HashSet<>(Collections.singletonList("*")));
    cors.setAllowedCredentials(true);
    getServices().add(cors);
  }

  public Properties getProperties() {
    return properties;
  }

  private boolean checkProperties(Properties properties) {
    String[] propertiesSet = {
      SapsPropertiesConstants.SUBMISSION_REST_SERVER_PORT,
      SapsPropertiesConstants.Openstack.ObjectStoreService.KEY,
      SapsPropertiesConstants.PERMANENT_STORAGE_TASKS_DIR,
      SapsPropertiesConstants.Openstack.IdentityService.API_URL,
      SapsPropertiesConstants.Openstack.PROJECT_ID,
      SapsPropertiesConstants.Openstack.USER_ID,
      SapsPropertiesConstants.Openstack.USER_PASSWORD
    };

    return SapsPropertiesUtil.checkProperties(properties, propertiesSet);
  }

  public void startServer() throws Exception {
    Integer restServerPort =
        Integer.valueOf(
            (String) properties.get(SapsPropertiesConstants.SUBMISSION_REST_SERVER_PORT));

    LOGGER.info("Starting service on port [ " + restServerPort + "]");

    ConnectorService corsService = new ConnectorService();
    this.getServices().add(corsService);

    this.restletComponent = new Component();
    this.restletComponent.getServers().add(Protocol.HTTP, restServerPort);
    this.restletComponent.getClients().add(Protocol.FILE);
    this.restletComponent.getDefaultHost().attach(this);

    this.restletComponent.start();
  }

  public void stopServer() throws Exception {
    this.restletComponent.stop();
  }

  @Override
  /** This function define application routes */
  public Restlet createInboundRoot() {
    Router router = new Router(getContext());
    router.attach("/", MainResource.class);
    router.attach("/ui/{requestPath}", MainResource.class);
    router.attach(
        "/static",
        new Directory(getContext(), "file:///" + new File(DB_WEB_STATIC_ROOT).getAbsolutePath()));
    router.attach("/users", UserResource.class);
    router.attach("/processings", ImageResource.class);
    router.attach("/images/{imgName}", ImageResource.class);
    router.attach("/regions/details", RegionResource.class);
    router.attach("/regions/search", RegionResource.class);
    router.attach("/email", EmailResource.class);
    router.attach("/links", LinkResource.class);
    router.attach("/tasks/{id}", TaskResource.class);
    return router;
  }

  /**
   * This function gets {@code SapsImage} list in {@code Catalog}.
   *
   * @return {@code SapsImage} list
   */
  public List<SapsImage> getTasks() {
    return submissionDispatcher.getAllTasks();
  }

  /**
   * This function gets tasks with specific state in Catalog.
   *
   * @param state task state to be searched
   * @return tasks list with specific state
   * @throws SQLException
   */
  public List<SapsImage> getTasksInState(ImageTaskState state) throws SQLException {
    return this.submissionDispatcher.getTasksByState(state);
  }

  /**
   * This function get saps image with specific id in Catalog.
   *
   * @param taskId task id to be searched
   * @return saps image with specific id
   * @throws SQLException
   */
  public SapsImage getTask(String taskId) {
    return submissionDispatcher.getTaskById(taskId);
  }

  /**
   * This function add new tasks in Catalog.
   *
   * @param lowerLeftLatitude lower left latitude (coordinate)
   * @param lowerLeftLongitude lower left longitude (coordinate)
   * @param upperRightLatitude upper right latitude (coordinate)
   * @param upperRightLongitude upper right longitude (coordinate)
   * @param initDate initial date
   * @param endDate end date
   * @param inputdownloadingPhaseTag inputdownloading phase tag
   * @param preprocessingPhaseTag preprocessing phase tag
   * @param processingPhaseTag processing phase tag
   * @param priority priority of new tasks
   * @param email user email
   */
  public List<String> addNewTasks(
      String lowerLeftLatitude,
      String lowerLeftLongitude,
      String upperRightLatitude,
      String upperRightLongitude,
      Date initDate,
      Date endDate,
      String inputdownloadingPhaseTag,
      String preprocessingPhaseTag,
      String processingPhaseTag,
      String priority,
      String email)
      throws Exception {
    return submissionDispatcher.addTasks(
        lowerLeftLatitude,
        lowerLeftLongitude,
        upperRightLatitude,
        upperRightLongitude,
        initDate,
        endDate,
        inputdownloadingPhaseTag,
        preprocessingPhaseTag,
        processingPhaseTag,
        Integer.parseInt(priority),
        email);
  }

  /** It creates new User in {@code Catalog}. */
  public void createUser(
      String userEmail,
      String userName,
      String userPass,
      boolean userState,
      boolean userNotify,
      boolean adminRole) {
    submissionDispatcher.addUser(userEmail, userName, userPass, userState, userNotify, adminRole);
  }

  /** It gets {@code SapsUser} in {@code Catalog}. */
  public SapsUser getUser(String userEmail) {
    return submissionDispatcher.getUser(userEmail);
  }

  /**
   * It searches processed {@code SapsImage} list from area (between latitude and longitude
   * coordinates) between {@param initDate} and {@param endDate} with {@param
   * inputdownloadingPhaseTag}, {@param preprocessingPhaseTag} and {@param processingPhaseTag} tags.
   *
   * @param lowerLeftLatitude lower left latitude (coordinate)
   * @param lowerLeftLongitude lower left longitude (coordinate)
   * @param upperRightLatitude upper right latitude (coordinate)
   * @param upperRightLongitude upper right longitude (coordinate)
   * @param initDate initial date
   * @param endDate end date
   * @param inputdownloadingPhaseTag inputdownloading phase tag
   * @param preprocessingPhaseTag preprocessing phase tag
   * @param processingPhaseTag processing phase tag
   * @return processed {@code SapsImage} list following description
   */
  public List<SapsImage> searchProcessedTasks(
      String lowerLeftLatitude,
      String lowerLeftLongitude,
      String upperRightLatitude,
      String upperRightLongitude,
      Date initDate,
      Date endDate,
      String inputdownloadingPhaseTag,
      String preprocessingPhaseTag,
      String processingPhaseTag) {
    return submissionDispatcher.getProcessedTasks(
        lowerLeftLatitude,
        lowerLeftLongitude,
        upperRightLatitude,
        upperRightLongitude,
        initDate,
        endDate,
        inputdownloadingPhaseTag,
        preprocessingPhaseTag,
        processingPhaseTag);
  }

  public List<SapsImage> getTasksOnGoingWithPagination(Integer page, Integer size, String sortField, String sortOrder) {
    return submissionDispatcher.getTasksOnGoingWithPagination(page, size, sortField, sortOrder);
  }

  public Integer getTasksCountOnGoing() {
    return submissionDispatcher.getTasksCountOnGoing();
  }

  public List<SapsImage> getTasksCompletedWithPagination(Integer page, Integer size, String sortField, String sortOrder) {
    return submissionDispatcher.getTasksCompletedWithPagination(page, size, sortField, sortOrder);
  }

  public Integer getTasksCountCompleted() {
    return submissionDispatcher.getTasksCountCompleted();
  }

}
