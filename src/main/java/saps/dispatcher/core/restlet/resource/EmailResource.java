/* (C)2020 */
package saps.dispatcher.core.restlet.resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import saps.dispatcher.interfaces.*;
import saps.dispatcher.core.email.TaskCompleteInfo;
import saps.dispatcher.core.email.TasksEmailSender;

public class EmailResource extends BaseResource {

  private static final Logger LOGGER = Logger.getLogger(EmailResource.class);

  // TODO update value this to "tasks_id[]" (requires a change to the Dashboard component)
  private static final String REQUEST_ATTR_PROCESSED_TASKS = "images_id[]";

  @Post
  public Representation sendTaskToEmail(Representation representation) {
    Form form = new Form(representation);

    String userEmail = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
    String userPass = form.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);
    String userEGI = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EGI, true);

    // FIXME I think that authenticateUser should throw an exception itself once
    // the authentication process hasn't worked... - by @raonismaneoto
    if (!authenticateUser(userEmail, userPass, userEGI) || userEmail.equals("anonymous"))
      throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);

    String[] tasksId = form.getValuesArray(REQUEST_ATTR_PROCESSED_TASKS, true);
    Properties properties = application.getProperties();

    try {
      // TODO check if it is possible to reuse permanent storage instead of always creating another
      // one
      PermanentStorage permanentStorage = createPermanentStorage(properties);

      String noReplyEmail = properties.getProperty(SapsPropertiesConstants.NO_REPLY_EMAIL);
      String noReplyPass = properties.getProperty(SapsPropertiesConstants.NO_REPLY_PASS);

      List<TaskCompleteInfo> tasks =
          buildTasksListByTaskIdsList(permanentStorage, Arrays.asList(tasksId));

      TasksEmailSender emailBuilder =
          new TasksEmailSender(noReplyEmail, noReplyPass, userEmail, tasks);
      Thread thread = new Thread(emailBuilder);
      thread.start();

      return new StringRepresentation("Email will be sent soon.", MediaType.TEXT_PLAIN);
    } catch (NullPointerException e) {
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
    // FIXME replace this to a more flexible approach to avoid if/switchs. something
    // akin the RAS approach to load the plugins - by @thiagomanel and @raonismaneoto
    if (PermanentStorageType.SWIFT.toString().equalsIgnoreCase(permanentStorageType)) {
      return new SwiftPermanentStorage(properties);
    } else if (PermanentStorageType.NFS.toString().equalsIgnoreCase(permanentStorageType)) {
      return new NfsPermanentStorage(properties);
    }
    throw new IOException("Failed to recognize type of permanent storage");
  }

  private List<TaskCompleteInfo> buildTasksListByTaskIdsList(
      PermanentStorage permanentStorage, List<String> tasksId)
      throws IOException, TaskNotFoundException {
    List<TaskCompleteInfo> tasksCompleteInfo = new LinkedList<>();
    for (String taskId : tasksId) {
      SapsImage currentTask = application.getTask(taskId);
      List<AccessLink> currentTaskAccessLinks = permanentStorage.generateAccessLinks(currentTask);
      TaskCompleteInfo taskCompleteInfo = new TaskCompleteInfo(currentTask, currentTaskAccessLinks);
      tasksCompleteInfo.add(taskCompleteInfo);
    }
    return tasksCompleteInfo;
  }
}
