package saps.dispatcher.core.restlet.resource;

import java.util.List;
import java.util.Properties;

import saps.dispatcher.interfaces.AccessLink;
import saps.dispatcher.interfaces.SapsImage;

public class SwiftPermanentStorage implements PermanentStorage {

    public SwiftPermanentStorage(Properties properties) {
    }

    @Override
    public List<AccessLink> generateAccessLinks(SapsImage sapsTask) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateAccessLinks'");
    }

}
