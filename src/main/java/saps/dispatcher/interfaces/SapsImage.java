package saps.dispatcher.interfaces;

import java.util.Collection;
import java.util.Date;

import org.json.JSONException;

public interface SapsImage {

    public String getId();

    public String getTaskId();

    public String getRegion();

    public String getCollectionTierName();

    public Date getImageDate();

    public Collection toJSON()throws JSONException;

}
