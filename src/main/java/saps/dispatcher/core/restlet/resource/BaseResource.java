/* (C)2020 */
package saps.dispatcher.core.restlet.resource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.resource.ServerResource;
import saps.common.core.model.SapsUser;
import saps.dispatcher.core.restlet.DatabaseApplication;

public class BaseResource extends ServerResource {

  private static final Logger LOGGER = Logger.getLogger(BaseResource.class);

  protected DatabaseApplication application;

  public BaseResource() {
    application = (DatabaseApplication) getApplication();
  }

  protected boolean authenticateUser(String userEmail, String userPass) {
    return authenticateUser(userEmail, userPass, false);
  }

  protected boolean authenticateUser(String userEmail, String userPass, boolean mustBeAdmin) {
    LOGGER.debug(
        "Trying to authenticate the user [" + userEmail + "] with password [" + userPass + "]");

    if (userEmail == null || userEmail.isEmpty() || userPass == null || userPass.isEmpty()) {
      LOGGER.error("User email or user password was null.");
      return false;
    }

    SapsUser user = application.getUser(userEmail);
    String md5Pass = DigestUtils.md5Hex(userPass);

    LOGGER.debug("Getting user [" + user + "] from Catalog");

    LOGGER.debug(
        "Comparing user password in Catalog ["
            + user.getUserPassword()
            + "] to password ["
            + md5Pass
            + "]");

    if (user.getUserPassword().equals(md5Pass) && user.isEnable()) {
      if (mustBeAdmin && !user.getAdminRole()) {
        // the user must be an admin and the logged user is not
        LOGGER.error("Admin level account needed for this action.");
        return false;
      }
      LOGGER.debug("User [" + userEmail + "] is allowed to authenticate");
      return true;
    }
    LOGGER.error("No user with this email or password mismatch.");
    return false;
  }

  String extractCoordinate(Form form, String name, int index) {
    String data[] = form.getValuesArray(name + "[]");
    return data[index];
  }

  Date extractDate(Form form, String name) throws ParseException {
    String data = form.getFirstValue(name);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    return dateFormat.parse(data);
  }
}
