package saps.dispatcher.interfaces;

public interface SapsPropertiesConstants {

    String DATASET_LT5_TYPE = null;
    String DATASET_LE7_TYPE = null;
    String DATASET_LC8_TYPE = null;
    String SUBMISSION_REST_SERVER_PORT = null;
    String PERMANENT_STORAGE_TASKS_DIR = null;
    String SAPS_PERMANENT_STORAGE_TYPE = null;
    String NO_REPLY_EMAIL = null;
    String NO_REPLY_PASS = null;

    public final class Openstack {

        public static final String PROJECT_ID = "openstack_project_id";
        public static final String USER_ID = "openstack_user_id";
        public static final String USER_PASSWORD = "openstack_user_password";

        public final class IdentityService {
        public static final String API_URL = "openstack_identity_service_api_url";
        }

        public final class ObjectStoreService {
        public static final String API_URL = "openstack_object_store_service_api_url";
        public static final String CONTAINER_NAME = "openstack_object_store_service_container_name";
        public static final String KEY = "openstack_object_store_service_key";
        }
    }

}
