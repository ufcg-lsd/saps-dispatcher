/* (C)2020 */
package saps.dispatcher.core.email;

import com.google.gson.Gson;
import java.util.*;
import javax.mail.MessagingException;
import org.apache.log4j.Logger;

public class TasksEmailSender implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(TasksEmailSender.class);
  private static final String EMAIL_TITLE = "[SAPS] Results of selected tasks";

  private final List<TaskCompleteInfo> tasksEmail;
  private final Gson gson;
  private final String userEmail;
  private final String noReplyEmail;
  private final String noReplyPass;

  public TasksEmailSender(
      String noReplyEmail,
      String noReplyPass,
      String userEmail,
      List<TaskCompleteInfo> tasksEmail) {
    if (isInvalidFields(noReplyEmail, noReplyPass, userEmail, tasksEmail))
      throw new IllegalArgumentException("Illegals arguments to use the send email feature");

    this.gson = new Gson();
    this.noReplyEmail = noReplyEmail;
    this.noReplyPass = noReplyPass;
    this.userEmail = userEmail;
    this.tasksEmail = tasksEmail;
  }

  private boolean isInvalidFields(
      String noReplyEmail,
      String noReplyPass,
      String userEmail,
      List<TaskCompleteInfo> tasksEmail) {
    return noReplyEmail.trim().isEmpty()
        || Objects.isNull(noReplyEmail)
        || noReplyPass.trim().isEmpty()
        || Objects.isNull(noReplyPass)
        || userEmail.trim().isEmpty()
        || Objects.isNull(userEmail)
        || Objects.isNull(tasksEmail);
  }

  @Override
  public void run() {
    try {
      LOGGER.info("Creating representation for tasks list: " + tasksEmail);

      String tasksEmailInString = gson.toJson(tasksEmail);

      LOGGER.debug("Tasks JSON array in String representation: " + tasksEmailInString);

      GoogleMail.Send(noReplyEmail, noReplyPass, userEmail, EMAIL_TITLE, tasksEmailInString);
    } catch (MessagingException e) {
      LOGGER.error("Error while send email", e);
    }
  }
}
