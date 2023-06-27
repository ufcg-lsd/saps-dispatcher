package saps.dispatcher.core;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import saps.dispatcher.interfaces.Catalog;
import saps.dispatcher.interfaces.CatalogUtils;
import saps.dispatcher.interfaces.Dispatcher;
import saps.dispatcher.interfaces.ImageTaskState;
import saps.dispatcher.interfaces.SapsImage;
import saps.dispatcher.interfaces.SapsPropertiesConstants;
import saps.dispatcher.interfaces.SapsUser;
import saps.dispatcher.interfaces.SapsUserJob;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.print.attribute.standard.JobState;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(PowerMockRunner.class)
@PrepareForTest({CatalogUtils.class})
public class SubmissionDispatcherTest implements Dispatcher {

    Dispatcher submissionDispatcher;
    Properties properties;

	
	@Mock		
	Catalog catalog = mock(Catalog.class);

    @Mock
    SapsUser user = mock(SapsUser.class);

    @Mock 
    SapsPropertiesConstants sapsPropertiesConstants = mock(SapsPropertiesConstants.class);
	
	@Mock
	SapsImage sapsImage1 = mock(SapsImage.class);
	
	@Mock
	SapsImage sapsImage2 = mock(SapsImage.class);

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        properties = new Properties();
    }

    @Test
    public List<String> createJobSubmission(String lowerLeftLatitude, String lowerLeftLongitude,
            String upperRightLatitude, String upperRightLongitude, Date initDate, Date endDate,
            String inputdownloadingPhaseTag, String preprocessingPhaseTag, String processingPhaseTag, int priority,
            String userEmail, String label) throws Exception {

         return null;
    }

    @Override
    public List<String> createJobsTasks(String lowerLeftLatitude, String lowerLeftLongitude, String upperRightLatitude,
            String upperRightLongitude, Date initDate, Date endDate, String inputdownloadingPhaseTag,
            String preprocessingPhaseTag, String processingPhaseTag, int priority, String userEmail) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createJobsTasks'");
    }

    @Override
    public SapsImage getTask(String taskId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTask'");
    }

    @Override
    public Integer getCountTasks(String search, ImageTaskState state) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCountTasks'");
    }

    @Override
    public SapsUser getUser(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public void addUser(String email, String name, String password, boolean state, boolean notify, boolean adminRole) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addUser'");
    }

    @Override
    public List<SapsImage> getProcessedTasks(String lowerLeftLatitude, String lowerLeftLongitude,
            String upperRightLatitude, String upperRightLongitude, Date initDate, Date endDate,
            String inputdownloadingPhaseTag, String preprocessingPhaseTag, String processingPhaseTag) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProcessedTasks'");
    }

    @Override
    public List<SapsUserJob> getAllJobs(JobState state, String search, Integer page, Integer size, String sortField,
            String sortOrder, boolean withoutTasks, boolean recoverOngoing, boolean recoverCompleted) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllJobs'");
    }

    @Override
    public Integer getJobsCount(JobState state, String search, boolean recoverOngoing, boolean recoverCompleted) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getJobsCount'");
    }

    @Override
    public List<SapsImage> getJobTasks(String jobId, ImageTaskState state, String search, Integer page, Integer size,
            String sortField, String sortOrder, boolean recoverOngoing, boolean recoverCompleted) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getJobTasks'");
    }

}