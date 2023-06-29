package saps.dispatcher.core;

import java.sql.Date;
import java.util.List;

import saps.dispatcher.interfaces.Catalog;
import saps.dispatcher.interfaces.CatalogUtils;
import saps.dispatcher.interfaces.Dispatcher;
import saps.dispatcher.interfaces.ImageTaskState;
import saps.dispatcher.interfaces.JobState;
import saps.dispatcher.interfaces.SapsImage;
import saps.dispatcher.interfaces.SapsPropertiesConstants;
import saps.dispatcher.interfaces.SapsUser;
import saps.dispatcher.interfaces.SapsUserJob;
import saps.dispatcher.utils.RegionUtil;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(PowerMockRunner.class)
@PrepareForTest({CatalogUtils.class, RegionUtil.class})
public class SubmissionDispatcherTest {

    Dispatcher submissionDispatcher;
    Properties properties;
    RegionUtil regionUtil;
	
	@Mock		
	Catalog catalog = mock(Catalog.class);

    @Mock
    SapsUser user = mock(SapsUser.class);

    @Mock 
    SapsPropertiesConstants sapsPropertiesConstants = mock(SapsPropertiesConstants.class);

    @Mock 
    SapsUserJob sapsUserJob1 = mock(SapsUserJob.class);

    @Mock
    SapsUserJob sapsUserJob2 = mock(SapsUserJob.class);
	
	@Mock
	SapsImage sapsImage1 = mock(SapsImage.class);
	
	@Mock
	SapsImage sapsImage2 = mock(SapsImage.class);

    @Mock
    SapsUserJob sapsJob = mock(SapsUserJob.class);

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        properties = new Properties();
        regionUtil = new RegionUtil();
        submissionDispatcher = new SubmissionDispatcher(catalog);
    }

    @Test 
    public void testCreateJobSubmission() {
        PowerMockito.mockStatic(CatalogUtils.class);
        PowerMockito.mockStatic(RegionUtil.class);

        String lowerLeftLatitude = "37.7749";  
        String lowerLeftLongitude = "-122.4194";  
        String upperRightLatitude = "37.8044";  
        String upperRightLongitude = "-122.4080";  

        Date initDate = new Date(0);  
        Date endDate = new Date(System.currentTimeMillis() + 86400000); 

        String inputdownloadingPhaseTag = "input_download";
        String preprocessingPhaseTag = "preprocessing";
        String processingPhaseTag = "processing";

        int priority = 1;  

        String userEmail = "example@example.com";  
        String label = "example_label";  

        Set<String> regions = RegionUtil.regionsFromArea(
            lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
        regions.add("x");   

        List<String> taskIds = new ArrayList<>();

        when(CatalogUtils.addNewTask(catalog, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, endDate, priority, 
        userEmail, inputdownloadingPhaseTag, inputdownloadingPhaseTag, preprocessingPhaseTag, preprocessingPhaseTag, preprocessingPhaseTag, 
        processingPhaseTag, label)).thenReturn(sapsImage2);

        when (sapsImage2.getId()).thenReturn("2");
        
        taskIds.add(sapsImage2.getId());

        try {
            List<String> result = submissionDispatcher.createJobSubmission(lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, 
            initDate, endDate, inputdownloadingPhaseTag, preprocessingPhaseTag, processingPhaseTag, priority, userEmail, label);

            assertEquals(result, taskIds);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Test 
    public void testGetProcessedTasks() {
        
        String lowerLeftLatitude = "x";
        String lowerLeftLongitude = "y";
        String upperRightLatitude = "z";
        String upperRightLongitude = "w";
        Date initDate = new Date(0);
        Date endDate = new Date(1);
        String inputdownloadingPhaseTag = "in";
        String preprocessingPhaseTag = "prep";
        String processingPhaseTag = "pro";

        PowerMockito.mockStatic(CatalogUtils.class);
        PowerMockito.mockStatic(RegionUtil.class);

        List<SapsImage> processedTasks = new ArrayList<>();
        processedTasks.add(sapsImage1);

        Set<String> regions = RegionUtil.regionsFromArea(lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
        regions.add("x");

        when(RegionUtil.regionsFromArea(lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude)).thenReturn(regions);
        System.out.println(regions.toString());

        when(CatalogUtils.getProcessedTasks(eq(catalog), anyString(), eq(initDate), eq(endDate), eq(inputdownloadingPhaseTag), 
        eq(preprocessingPhaseTag), eq(processingPhaseTag), anyString())).thenReturn(processedTasks);

        List<SapsImage> result = submissionDispatcher.getProcessedTasks(lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, 
      initDate, endDate, inputdownloadingPhaseTag, preprocessingPhaseTag, processingPhaseTag);

        assertEquals(processedTasks, result);

        assertFalse(result.contains(sapsImage2));
        assertTrue(result.contains(sapsImage1));    
    }

    @Test
    public void testGetJobsCount() {
        String search = "test";
        JobState jobState = JobState.CREATED;
        boolean recoverOngoing = true;
        boolean recoverCompleted = false;
        String message = "get amount of jobs";

        PowerMockito.mockStatic(CatalogUtils.class);

        ArrayList<SapsUserJob> jobs = new ArrayList<>();
        jobs.add(sapsUserJob1);
        jobs.add(sapsUserJob2);
        
        when(CatalogUtils.getUserJobsCount(catalog, jobState, search, recoverOngoing, recoverCompleted, message)).thenReturn(jobs.size());
    
        Integer result = submissionDispatcher.getJobsCount(jobState, search, recoverOngoing, recoverCompleted);
        assertTrue(jobs.size() == result);
        
    }
    
    @Test
    public void testGetAllJobs() {
        List<SapsUserJob> mockedJobs = new ArrayList<>();
        List<SapsUserJob> retrievedJobs = new ArrayList<>();

        SapsUserJob job1 = Mockito.mock(SapsUserJob.class);
        SapsUserJob job2 = Mockito.mock(SapsUserJob.class);
        SapsUserJob job3 = Mockito.mock(SapsUserJob.class);

        mockedJobs.add(job1);
        mockedJobs.add(job2);
        mockedJobs.add(job3);

        PowerMockito.mockStatic(CatalogUtils.class);

        when(CatalogUtils.getUserJobs(catalog, null, null, null, null, null, null, false, false, false, "get jobs")).thenReturn(mockedJobs);

        retrievedJobs = submissionDispatcher.getAllJobs(null, null, null, null, null, null, false, false, false);

        assertArrayEquals(mockedJobs.toArray(), retrievedJobs.toArray());
    }

    @Test
    public void testGetJobTasksOnGoing() {
        String jobId = "123";
        ImageTaskState state = ImageTaskState.ONGOING;
        String search = "test";
        Integer page = 1;
        Integer size = 10;
        String sortField = "date";
        String sortOrder = "asc";
        boolean recoverOngoing = false;
        boolean recoverCompleted = false;
        String message = "get job tasks";

        PowerMockito.mockStatic(CatalogUtils.class);

        List<SapsImage> tasks = new ArrayList<SapsImage>();
        tasks.add(sapsImage1);
        tasks.add(sapsImage2);

        when(CatalogUtils.getUserJobTasks(catalog, jobId, state, search, page, size, sortField,
         sortOrder, recoverOngoing, recoverCompleted, message)).thenReturn(tasks);
        
        List<SapsImage> result = submissionDispatcher.getJobTasks(jobId, state, search, page, size, sortField, 
        sortOrder, recoverOngoing, recoverCompleted);

        assertEquals(tasks, result);

    }

    @Test
    public void testGetJobTasksOnGoingFlag() {
        String jobId = "123";
        ImageTaskState state = ImageTaskState.ONGOING;
        String search = "test";
        Integer page = 1;
        Integer size = 10;
        String sortField = "date";
        String sortOrder = "asc";
        boolean recoverOngoing = true;
        boolean recoverCompleted = false;
        String message = "get job tasks";

        PowerMockito.mockStatic(CatalogUtils.class);

        List<SapsImage> tasks = new ArrayList<SapsImage>();
        tasks.add(sapsImage1);
        tasks.add(sapsImage2);

        when(CatalogUtils.getUserJobTasks(catalog, jobId, state, search, page, size, sortField,
         sortOrder, recoverOngoing, recoverCompleted, message)).thenReturn(tasks);
        
        List<SapsImage> result = submissionDispatcher.getJobTasks(jobId, state, search, page, size, sortField, 
        sortOrder, recoverOngoing, recoverCompleted);

        assertEquals(tasks, result);

    }

    @Test
    public void testGetJobTasksCompletedFlag() {
        String jobId = "123";
        ImageTaskState state = ImageTaskState.ONGOING;
        String search = "test";
        Integer page = 1;
        Integer size = 10;
        String sortField = "date";
        String sortOrder = "asc";
        boolean recoverOngoing = false;
        boolean recoverCompleted = true;
        String message = "get job tasks";

        PowerMockito.mockStatic(CatalogUtils.class);

        List<SapsImage> tasks = new ArrayList<SapsImage>();
        tasks.add(sapsImage1);
        tasks.add(sapsImage2);

        when(CatalogUtils.getUserJobTasks(catalog, jobId, state, search, page, size, sortField,
         sortOrder, recoverOngoing, recoverCompleted, message)).thenReturn(tasks);
        
        List<SapsImage> result = submissionDispatcher.getJobTasks(jobId, state, search, page, size, sortField, 
        sortOrder, recoverOngoing, recoverCompleted);

        assertEquals(tasks, result);

    }

    @Test
    public void testGetUser() {
        String email = "teste@email.com";
        PowerMockito.mockStatic(CatalogUtils.class);

        when(CatalogUtils.getUser(catalog, 
        email, 
        "get user [" + email + "] information")
        ).thenReturn(user);

        SapsUser newUser = submissionDispatcher.getUser(email);

        assertEquals(user, newUser);

    }

    @Test
    public void testGetTask() {
        String taskId = "x";
        PowerMockito.mockStatic(CatalogUtils.class);

        when(CatalogUtils.getTaskById(catalog, 
        taskId, 
        "gets task with id [" + taskId + "]"
        )).thenReturn(sapsImage1);

        SapsImage result = submissionDispatcher.getTask(taskId);

        assertEquals(sapsImage1, result);
        
    }

}