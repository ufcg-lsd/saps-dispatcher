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

}
