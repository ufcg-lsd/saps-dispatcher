/* (C)2020 */
package saps.dispatcher.core.email;

import com.google.gson.Gson;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import saps.common.core.model.SapsImage;
import saps.common.core.model.enums.ImageTaskState;
import saps.common.core.storage.AccessLink;

public class TaskCompleteInfoTest {

  private Gson jsonUtil;
  private SapsImage task;
  private static final String MOCK_ID = "task-email-id";
  private static final String MOCK_IMAGE_REGION = "task-image-region";
  private static final Date MOCK_IMAGE_DATE = new Date();
  private static final List<AccessLink> MOCK_ACCESS_LINKS = new LinkedList<>();

  @Before
  public void setUp() {
    jsonUtil = new Gson();
    task =
        new SapsImage(
            MOCK_ID,
            "",
            MOCK_IMAGE_REGION,
            MOCK_IMAGE_DATE,
            ImageTaskState.ARCHIVED,
            SapsImage.NONE_ARREBOL_JOB_ID,
            SapsImage.NONE_FEDERATION_MEMBER,
            0,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            new Timestamp(1),
            new Timestamp(1),
            "",
            "");
  }

  @Test
  // TODO upgrade this test
  public void testToJson() {
    TaskCompleteInfo taskCompleteInfo = new TaskCompleteInfo(task, MOCK_ACCESS_LINKS);

    String taskCompleteInfoInString = jsonUtil.toJson(taskCompleteInfo);
    Assert.assertNotNull(taskCompleteInfoInString);
    jsonUtil.fromJson(taskCompleteInfoInString, Object.class);
  }
}
