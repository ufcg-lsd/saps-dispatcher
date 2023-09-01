package saps.dispatcher.interfaces;

import java.util.List;
import java.util.Date;
import saps.common.core.model.SapsImage;
import saps.common.core.model.SapsUser;

public interface Dispatcher {

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
  public List<String> addTasks(
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
    String userEmail)
    throws Exception;

     /**
     * Retrieves a task from the catalog based on the specified task ID.
     *
     * @param taskId the ID of the task to retrieve
     * @return the SapsImage representing the retrieved task
     */
     SapsImage getTask(String taskId);

    /**
     * It gets {@code SapsUser} in {@code Catalog}.
     *
     * @return an {@code SapsUser} with equal email
     */
     SapsUser getUser(String email);

     
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
    void addUser(String email, String name, String password, boolean state, boolean notify, boolean adminRole);


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
    List<SapsImage> getProcessedTasks(
        String lowerLeftLatitude,
        String lowerLeftLongitude,
        String upperRightLatitude,
        String upperRightLongitude,
        Date initDate,
        Date endDate,
        String inputdownloadingPhaseTag,
        String preprocessingPhaseTag,
        String processingPhaseTag);
    
    
}
