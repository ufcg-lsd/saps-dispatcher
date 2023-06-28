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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
public class SubmissionDispatcherTest {

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
    public void testCreateJobSubmission() throws Exception {

        String lowerLeftLatitude = "10.0";
        String lowerLeftLongitude = "20.0";
        String upperRightLatitude = "30.0";
        String upperRightLongitude = "40.0";
        Date initDate = new Date(0);
        Date endDate = new Date(1);
        String inputdownloadingPhaseTag = "inputTag";
        String preprocessingPhaseTag = "preprocessingTag";
        String processingPhaseTag = "processingTag";
        int priority = 1;
        String userEmail = "test@example.com";
        String label = "Test Job";

        List<String> taskIds = new ArrayList<>();
            when(submissionDispatcher.createJobSubmission(
                lowerLeftLatitude,
                lowerLeftLongitude,
                upperRightLatitude,
                upperRightLongitude,
                initDate,
                endDate,
                inputdownloadingPhaseTag,
                preprocessingPhaseTag,
                processingPhaseTag,
                priority,
                userEmail,
                label)
        ).thenReturn(taskIds);

         List<String> result = submissionDispatcher.createJobSubmission(
                lowerLeftLatitude,
                lowerLeftLongitude,
                upperRightLatitude,
                upperRightLongitude,
                initDate,
                endDate,
                inputdownloadingPhaseTag,
                preprocessingPhaseTag,
                processingPhaseTag,
                priority,
                userEmail,
                label
        );

        assertEquals(taskIds, result);
    }

    @Test
    public void testGetTask() {
        // TODO Auto-generated method stub
        submissionDispatcher = new SubmissionDispatcher(catalog);
    }

    @Test
    public void testGetCountTasks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCountTasks'");
    }

    @Test
    public void testGetUser() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Test
    public void testAddUser() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addUser'");
    }

    @Test
    public void testGetProcessedTasks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProcessedTasks'");
    }

    @Test
    public void testGetAllJobs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllJobs'");
    }

    @Test
    public void testGetJobsCount() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getJobsCount'");
    }

    @Test
    public void testGetJobTasks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getJobTasks'");
    }

}