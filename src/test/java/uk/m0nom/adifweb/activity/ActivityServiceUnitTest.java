package uk.m0nom.adifweb.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.m0nom.adifproc.activity.Activity;
import uk.m0nom.adifproc.activity.ActivityDatabase;
import uk.m0nom.adifproc.activity.ActivityDatabaseService;
import uk.m0nom.adifproc.activity.ActivityType;
import uk.m0nom.adifproc.activity.sota.SotaInfo;
import uk.m0nom.adifweb.ApplicationConfiguration;
import uk.m0nom.adifweb.domain.ActivitySearchResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceUnitTest {

    @Mock
    private ApplicationConfiguration applicationConfiguration;
    
    @Mock
    private ActivityDatabaseService activityDatabaseService;
    
    @Mock
    private ActivityDatabase sotaDatabase;
    
    private ActivityService activityService;

    @BeforeEach
    public void setUp() {
        when(applicationConfiguration.getActivityDatabases()).thenReturn(activityDatabaseService);
        activityService = new ActivityService(applicationConfiguration);
    }

    @Test
    public void testGetMatchingActivitiesWithLuceneSuccess() {
        // Create test activities
        List<Activity> testActivities = createTestActivities();
        
        // Mock the activity database service
        when(activityDatabaseService.getDatabase(ActivityType.SOTA)).thenReturn(sotaDatabase);
        when(sotaDatabase.size()).thenReturn(testActivities.size());
        when(sotaDatabase.getValues()).thenReturn(testActivities);
        
        // Mock individual activity lookups
        for (Activity activity : testActivities) {
            when(sotaDatabase.get(activity.getRef())).thenReturn(activity);
        }
        
        // Mock other activity types to return empty/null
        for (ActivityType type : ActivityType.values()) {
            if (type != ActivityType.SOTA) {
                when(activityDatabaseService.getDatabase(eq(type))).thenReturn(null);
            }
        }
        
        // Test the search
        ActivitySearchResult result = activityService.getMatchingActivities("G/LD");
        
        // Verify results
        assertNotNull(result, "Expected result object even if no matches");
        assertTrue(result.hasMatches(), "Expected to find matching activities for G/LD");
        
        // Verify that G/LD references are found
        long gldReferences = result.getMatches().stream()
            .filter(activity -> activity.getRef().contains("G/LD"))
            .count();
        
        assertTrue(gldReferences >= 2, 
            String.format("Expected at least 2 G/LD references, but found %d", gldReferences));
        
        // Verify specific activities are found
        boolean foundGLD001 = result.getMatches().stream()
            .anyMatch(activity -> "G/LD-001".equals(activity.getRef()));
        assertTrue(foundGLD001, "Expected to find G/LD-001 (Scafell Pike)");
    }

    @Test
    public void testGetMatchingActivitiesWithFallback() throws Exception {
        // Setup for fallback scenario - no Lucene indexes available
        when(activityDatabaseService.getDatabase(any(ActivityType.class))).thenReturn(null);
        
        List<Activity> allActivities = createTestActivities();
        when(activityDatabaseService.findAllActivities()).thenReturn(allActivities);
        
        // Test the search
        ActivitySearchResult result = activityService.getMatchingActivities("Scafell");
        
        // Verify results using fallback method
        assertNotNull(result, "Expected result object even with fallback");
        
        // Should find activities matching "Scafell" in name
        boolean foundScafell = result.getMatches().stream()
            .anyMatch(activity -> activity.getName().contains("Scafell"));
        assertTrue(foundScafell, "Expected to find activity with 'Scafell' in name using fallback");
    }

    @Test
    public void testGetMatchingActivitiesNoResults() throws Exception {
        // Setup empty database
        when(activityDatabaseService.getDatabase(any(ActivityType.class))).thenReturn(null);
        when(activityDatabaseService.findAllActivities()).thenReturn(Collections.emptyList());
        
        // Test search for non-existent substring
        ActivitySearchResult result = activityService.getMatchingActivities("ZZZZNONEXISTENT");
        
        // Verify no matches
        assertNotNull(result, "Expected result object even for no matches");
        assertTrue(result.getMatches().isEmpty(), "Expected no matches for non-existent substring");
    }

    private List<Activity> createTestActivities() {
        List<Activity> activities = new ArrayList<>();
        
        // Create some test SOTA activities
        SotaInfo gld001 = new SotaInfo();
        gld001.setRef("G/LD-001");
        gld001.setName("Scafell Pike");
        activities.add(gld001);

        SotaInfo gld002 = new SotaInfo();
        gld002.setRef("G/LD-002");
        gld002.setName("Scafell");
        activities.add(gld002);

        SotaInfo gld003 = new SotaInfo();
        gld003.setRef("G/LD-003");
        gld003.setName("Helvellyn");
        activities.add(gld003);

        SotaInfo gld004 = new SotaInfo();
        gld004.setRef("G/LD-004");
        gld004.setName("Skiddaw");
        activities.add(gld004);
        
        SotaInfo gld005 = new SotaInfo();
        gld005.setRef("G/LD-005");
        gld005.setName("Great End");
        activities.add(gld005);

        return activities;
    }
}