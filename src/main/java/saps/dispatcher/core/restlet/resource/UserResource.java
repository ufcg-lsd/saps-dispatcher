/* (C)2020 */
package saps.dispatcher.core.restlet.resource;

import java.util.Properties;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import saps.common.core.model.SapsUser;

public class UserResource extends BaseResource {

  private static final Logger LOGGER = Logger.getLogger(UserResource.class);

  public static final String REQUEST_ATTR_USER_EMAIL = "userEmail";
  public static final String REQUEST_ATTR_USERNAME = "userName";
  public static final String REQUEST_ATTR_USERPASS = "userPass";
  public static final String REQUEST_ATTR_USER_EGI = "userEGI";
  private static final String REQUEST_ATTR_USERPASS_CONFIRM = "userPassConfirm";
  private static final String REQUEST_ATTR_USERNOTIFY = "userNotify";
  private static final String CREATE_USER_MESSAGE_OK = "User created successfully";
  private static final String CREATE_USER_ALREADY_EXISTS = "User already exists";
  private static final String EGI_SECRET_KEY = "user_egi_secret_key";

  public UserResource() {
    super();
  }

  @Post("?register")
  public Representation createUser(Representation entity) throws Exception {

    Form form = new Form(entity);

    String userEmail = form.getFirstValue(REQUEST_ATTR_USER_EMAIL);
    String userName = form.getFirstValue(REQUEST_ATTR_USERNAME);
    String userPass = form.getFirstValue(REQUEST_ATTR_USERPASS);
    String userPassConfirm = form.getFirstValue(REQUEST_ATTR_USERPASS_CONFIRM);
    String userNotify = form.getFirstValue(REQUEST_ATTR_USERNOTIFY);
    
    checkMandatoryAttributes(userName, userEmail, userPass, userPassConfirm);
    
    Properties properties = application.getProperties();
    String EGISecretKey = properties.getProperty(EGI_SECRET_KEY);  
    
    SapsUser user = null;

    try {
        user = application.getUser(userEmail);
    } catch (saps.catalog.core.exceptions.UserNotFoundException e) {
        user = null;
    }

    if (user != null && userPass.equals(EGISecretKey)) {
      LOGGER.debug("User [" + userEmail + "] successfully authenticated");
      return new StringRepresentation("Success");
    } 
    else {
      LOGGER.debug("Creating user with userEmail " + userEmail + " and userName " + userName);
    }

    try {
      String md5Pass = DigestUtils.md5Hex(userPass);
      boolean userState = userPass.equals(EGISecretKey); //If is an EGI user, automatically his status is enabled
      boolean notify = false;
      if (userNotify.equals("yes")) {
        notify = true;
      }
      application.createUser(userEmail, userName, md5Pass, userState, notify, false);
    } catch (Exception e) {        

      LOGGER.error("Error while creating user", e);

      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, CREATE_USER_ALREADY_EXISTS);
    }

    return new StringRepresentation(CREATE_USER_MESSAGE_OK, MediaType.TEXT_PLAIN);
  }

  @Post("?auth")
  public Representation doAuthentication(Representation entity) {
    Form form = new Form(entity);

    String user = form.getFirstValue(REQUEST_ATTR_USER_EMAIL, true);
    String pass = form.getFirstValue(REQUEST_ATTR_USERPASS, true);
    String userEGI = form.getFirstValue(REQUEST_ATTR_USER_EGI, true);

    if (authenticateUser(user, pass, userEGI)) {
      LOGGER.debug("User [" + user + "] successfully authenticated");
      return new StringRepresentation("Success");
    } else {
      LOGGER.debug("User [" + user + "] authentication failure");
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Incorrect user/password.");
    }
  }

  private void checkMandatoryAttributes(
      String userName, String userEmail, String userPass, String userPassConfirm) {
    if (userEmail == null
        || userEmail.isEmpty()
        || userName == null
        || userName.isEmpty()
        || userPass == null
        || userPass.isEmpty()
        || !userPass.equals(userPassConfirm)) {
      throw new ResourceException(HttpStatus.SC_BAD_REQUEST);
    }
  }
}
