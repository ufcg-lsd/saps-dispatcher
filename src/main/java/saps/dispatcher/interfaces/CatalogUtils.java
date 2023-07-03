package saps.dispatcher.interfaces;

import java.util.Date;
import java.util.List;

public interface CatalogUtils {

    static List<SapsImage> getTasksOngoingWithPagination(Catalog catalog, String search, Integer page, Integer size,
            String sortField, String sortOrder, String string) {
        return null;
    }

    static List<SapsImage> getTasksCompletedWithPagination(Catalog catalog, String search, Integer page, Integer size,
            String sortField, String sortOrder, String string) {
        return null;
    }

    static List<SapsImage> getTasks(Catalog catalog, ImageTaskState state) {
        return null;
    }

    static Integer getCountOngoingTasks(Catalog catalog, String search, String string) {
        return null;
    }

    static Integer getCountCompletedTasks(Catalog catalog, String search, String string) {
        return null;
    }

    static void addNewUser(Catalog catalog, String email, String name, String password, boolean state, boolean notify,
            boolean adminRole, String string) {
    }

    static SapsUser getUser(Catalog catalog, String email, String string) {
        return null;
    }

    static SapsImage addNewTask(Catalog catalog, String taskId, String dataset, String region, Date date, int priority,
            String userEmail, String inputdownloadingPhaseTag, String digestInputdownloading,
            String preprocessingPhaseTag, String digestPreprocessing, String processingPhaseTag,
            String digestProcessing, String string) {
        return null;
    }

    static SapsImage getTaskById(Catalog catalog, String taskId, String string) {
        return null;
    }

    static List<SapsImage> getProcessedTasks(Catalog catalog, String region, Date initDate, Date endDate,
            String inputdownloadingPhaseTag, String preprocessingPhaseTag, String processingPhaseTag, String string) {
        return null;
    }

    static void addNewUserJob(Catalog catalog, String jobId, String lowerLeftLatitude, String lowerLeftLongitude,
            String upperRightLatitude, String upperRightLongitude, String userEmail, String jobLabel, Date startDate,
            Date endDate, int priority, List<String> tasksIds, String string) {
    }

    static void insertJobTask(Catalog catalog, String taskId, String jobId, String string) {
    }

    static List<SapsUserJob> getUserJobs(Catalog catalog, JobState state, String search, Integer page, Integer size,
            String sortField, String sortOrder, boolean withoutTasks, boolean recoverOngoing, boolean recoverCompleted,
            String string) {
        return null;
    }

    static Integer getUserJobsCount(Catalog catalog, JobState state, String search, boolean recoverOngoing,
            boolean recoverCompleted, String string) {
        return null;
    }

    static Integer getUserJobTasksCount(Catalog catalog, String jobId, ImageTaskState state, String search,
            boolean recoverOngoing, boolean recoverCompleted, String string) {
        return null;
    }

    static List<SapsImage> getUserJobTasks(Catalog catalog, String jobId, ImageTaskState state, String search,
            Integer page, Integer size, String sortField, String sortOrder, boolean recoverOngoing,
            boolean recoverCompleted, String string) {
        return null;
    }

    static SapsLandsatImage validateLandsatImage(Catalog catalog, String region, Date date, String string) {
        return null;
    }

    static void addTimestampTask(Catalog catalog, SapsImage task) {
    }

}
