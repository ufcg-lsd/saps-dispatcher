/* (C)2020 */
package saps.dispatcher.core.email;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;
import saps.common.core.model.SapsImage;
import saps.common.core.storage.AccessLink;

public class TaskCompleteInfo {

  @SerializedName("id")
  private final String id;

  @SerializedName("image_region")
  private final String imageRegion;

  @SerializedName("image_collection_name")
  private final String imageCollectionName;

  @SerializedName("image_date")
  private final Date imageDate;

  @SerializedName("access_links")
  private final List<AccessLink> accessLinks;

  public TaskCompleteInfo(SapsImage sapsTask, List<AccessLink> accessLinks) {
    this.id = sapsTask.getTaskId();
    this.imageRegion = sapsTask.getRegion();
    this.imageCollectionName = sapsTask.getCollectionTierName();
    this.imageDate = sapsTask.getImageDate();
    this.accessLinks = accessLinks;
  }

  @Override
  public String toString() {
    return "TaskCompleteInfo {"
        + "id='"
        + id
        + '\''
        + ", imageRegion='"
        + imageRegion
        + '\''
        + ", imageCollectionName='"
        + imageCollectionName
        + '\''
        + ", imageDate="
        + imageDate
        + ", accessLinks="
        + accessLinks
        + '}';
  }
}
