/* (C)2020 */
package saps.dispatcher.core.restlet.resource;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import saps.common.core.model.SapsImage;
import saps.common.core.storage.AccessLink;
import saps.archiver.interfaces.PermanentStorage;
import saps.archiver.core.FSPermanentStorage;
import saps.common.core.storage.exceptions.TaskNotFoundException;
import saps.common.utils.SapsPropertiesConstants;

public class LinkResource extends BaseResource {

  private static final Logger LOGGER = Logger.getLogger(EmailResource.class);

  private static final String REQUEST_ATTR_TASK_ID = "taskId";

  private final Gson gson = new Gson();;

  @Get
  public Representation getTaskLinks(Representation representation) {
    Form form = new Form(representation);

    String userEmail = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
    String userPass = form.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);
    String userEGI = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EGI, true);

    if (!authenticateUser(userEmail, userPass, userEGI))
      throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);

    String taskId = form.getFirstValue(REQUEST_ATTR_TASK_ID, true);
    Properties properties = application.getProperties();

    try {
      SapsImage sapsTask = application.getTask(taskId);

      PermanentStorage permanentStorage = createPermanentStorage(properties);

      List<AccessLink> links = permanentStorage.generateAccessLinks(sapsTask);

      return new StringRepresentation(gson.toJson(links), MediaType.APPLICATION_JSON);
    } catch (TaskNotFoundException e) {
      LOGGER.error("Error while getting task by id", e);
    } catch (Exception e) {
      LOGGER.error("Error while create permanent storage", e);
    }
    return new StringRepresentation(
        "An error occurred while sending the email, please try again later.", MediaType.TEXT_PLAIN);
  }

  private PermanentStorage createPermanentStorage(Properties properties) throws Exception {
    String permanentStorageType =
        properties.getProperty(SapsPropertiesConstants.SAPS_PERMANENT_STORAGE_TYPE);
      return new FSPermanentStorage(properties); 
  }

}
