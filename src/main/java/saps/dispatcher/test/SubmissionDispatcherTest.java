import java.sql.SQLException;
import java.util.List;

import saps.dispatcher.interfaces.Dispatcher;
import saps.dispatcher.interfaces.ImageTaskState;
import saps.dispatcher.interfaces.SapsImage;

public class SubmissionDispatcherTest implements Dispatcher {

    @Override
    public List<SapsImage> getTasks(String search, Integer page, Integer size, String sortField, String sortOrder,
            ImageTaskState state) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTasks'");
    }


}