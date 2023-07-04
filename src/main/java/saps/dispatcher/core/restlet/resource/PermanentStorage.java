package saps.dispatcher.core.restlet.resource;

import java.util.List;

import saps.dispatcher.interfaces.AccessLink;
import saps.dispatcher.interfaces.SapsImage;

public interface PermanentStorage {

    List<AccessLink> generateAccessLinks(SapsImage sapsTask);

}
