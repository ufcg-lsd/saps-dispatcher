/* (C)2020 */
package saps.dispatcher.core;

import java.io.FileInputStream;
import java.util.Objects;
import java.util.Properties;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import saps.dispatcher.interfaces.*;
import saps.dispatcher.core.restlet.DatabaseApplication;

public class SubmissionDispatcherMain {

  private static final String ADMIN_EMAIL = "admin_email";
  private static final String ADMIN_USER = "admin_user";
  private static final String ADMIN_PASSWORD = "admin_password";
  private static final Logger LOGGER = Logger.getLogger(SubmissionDispatcherMain.class);

  public static void main(String[] args) throws Exception {

    String confPath = args[0];
    String executionTagsFilePath = args[1];

    if (Objects.isNull(confPath) || confPath.isEmpty()) {
      throw new IllegalArgumentException(
          "The path to the configuration file cannot be null or empty");
    }

    if (Objects.isNull(executionTagsFilePath) || executionTagsFilePath.isEmpty()) {
      throw new IllegalArgumentException(
          "The path to the execution tags file cannot be null or empty");
    }

    System.setProperty(SubmissionDispatcher.EXECUTION_TAGS_FILE_PATH_KEY, executionTagsFilePath);

    final Properties properties = new Properties();
    FileInputStream input = new FileInputStream(confPath);
    properties.load(input);

    DatabaseApplication databaseApplication = new DatabaseApplication(properties);
    databaseApplication.startServer();

    String userEmail = properties.getProperty(ADMIN_EMAIL);
    SapsUser user = databaseApplication.getUser(userEmail);
    if (user == null) {
      String userName = properties.getProperty(ADMIN_USER);
      String userPass = DigestUtils.md5Hex(properties.getProperty(ADMIN_PASSWORD));

      try {
        databaseApplication.createUser(userEmail, userName, userPass, true, false, true);
      } catch (Exception e) {
        LOGGER.error("Error while creating user", e);
      }
    }
  }
}
