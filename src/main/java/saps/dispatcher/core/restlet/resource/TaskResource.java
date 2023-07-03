/* (C)2020 */
package saps.dispatcher.core.restlet.resource;

import com.google.gson.Gson;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import saps.dispatcher.interfaces.SapsImage;

public class TaskResource extends BaseResource {

  private static final Logger LOGGER = Logger.getLogger(EmailResource.class);

  private static final String REQUEST_ATTR_TASK_ID = "taskId";

  private final Gson gson = new Gson();;

  @Get
  public Representation getTaskById(Representation representation) {
    Form form = new Form(representation);

    String userEmail = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
    String userPass = form.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);
    String userEGI = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EGI, true);

    // FIXME I think that authenticateUser should throw an exception itself once
    // the authentication process hasn't worked... - by @raonismaneoto
    if (!authenticateUser(userEmail, userPass, userEGI))
      throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);

    String taskId = getAttribute("id");

    SapsImage sapsTask = application.getTask(taskId);

    return new StringRepresentation(gson.toJson(sapsTask), MediaType.APPLICATION_JSON);
    // return new StringRepresentation("An error occurred while getting the task by id [" + taskId +
    // "], please try again later.", MediaType.TEXT_PLAIN);
  }
}
