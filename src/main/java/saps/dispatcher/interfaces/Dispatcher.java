package saps.dispatcher.interfaces;

import java.util.List;
import java.sql.SQLException;
import java.util.Date;

public interface Dispatcher {

     /**
     * Creates a job submission with the specified parameters and returns the list of task IDs associated with the job.
     *
     * @param lowerLeftLatitude the latitude of the lower left corner of the job area
     * @param lowerLeftLongitude the longitude of the lower left corner of the job area
     * @param upperRightLatitude the latitude of the upper right corner of the job area
     * @param upperRightLongitude the longitude of the upper right corner of the job area
     * @param initDate the initial date for the job
     * @param endDate the end date for the job
     * @param inputdownloadingPhaseTag the tag for the input downloading phase
     * @param preprocessingPhaseTag the tag for the preprocessing phase
     * @param processingPhaseTag the tag for the processing phase
     * @param priority the priority of the job
     * @param userEmail the email of the user associated with the job
     * @param label the label for the job
     * @return the list of task IDs associated with the created job
     * @throws Exception if an error occurs during the job creation process
     */
    List<String> createJobSubmission(
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
        String userEmail,
        String label)
        throws Exception;

    /**
     * This function get all saps user job in Catalog.
     * 
     * @param state              state of jobs
     * @param search             search string
     * @param page               page number
     * @param size               page size
     * @param sortField          sort field
     * @param sortOrder          sort order
     * @param withoutTasks       without tasks
     * @param recoverOngoing     if true, only ongoing jobs will be recovered
     * @param recoverCompleted   if true, only completed jobs will be recovered
     * @return list of jobs
     */
    List<SapsUserJob> getAllJobs(JobState state, String search, Integer page, Integer size, String sortField,
        String sortOrder, boolean withoutTasks, boolean recoverOngoing, boolean recoverCompleted);


    /**
     * This function get tha amount of all jobs in Catalog.
     * 
     * @param state              state of jobs
     * @param search             search string
     * @param recoverOngoing     if true, only ongoing jobs will be recovered
     * @param recoverCompleted   if true, only completed jobs will be recovered
     * @return amount of all jobs
     */
     Integer getJobsCount(JobState state, String search, boolean recoverOngoing, boolean recoverCompleted);


    /**
     * This function get the jobs tasks in Catalog based on the job id.
     *
     * @param jobId              job id to be searched
     * @param state              state of jobs
     * @param search             search string
     * @param page               page number
     * @param size               page size
     * @param sortField          sort field
     * @param sortOrder          sort order
     * @param recoverOngoing     if true, only ongoing tasks will be recovered
     * @param recoverCompleted   if true, only completed jobs will be recovered
     * @return saps user job with specific id
     * @throws SQLException
     */
     List<SapsImage> getJobTasks(String jobId, ImageTaskState state, String search, Integer page,
        Integer size, String sortField, String sortOrder, boolean recoverOngoing, boolean recoverCompleted);


     /**
     * Retrieves a task from the catalog based on the specified task ID.
     *
     * @param taskId the ID of the task to retrieve
     * @return the SapsImage representing the retrieved task
     */
     SapsImage getTask(String taskId);


     /**
     * Retrieves the count of tasks based on the specified search query and task state.
     *
     * @param search the search query to filter the tasks (can be null or empty for all tasks)
     * @param state the state of the tasks to count (ONGOING or COMPLETED)
     * @return the count of tasks that match the search query and task state, or null if the state is neither ONGOING nor COMPLETED
     */
     Integer getCountTasks(String search, ImageTaskState state);


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
