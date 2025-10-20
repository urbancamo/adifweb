package uk.m0nom.adifweb.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.m0nom.adifproc.activity.ActivityDatabaseService;
import uk.m0nom.adifweb.domain.ActivitySearchResult;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ActivityServiceTest {

    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private ActivityDatabaseService activityDatabaseService;
    
    @BeforeEach
    public void setUp() {
        // Ensure activity data is loaded before each test
        activityDatabaseService.loadData();
    }

    @Test
    public void testGetMatchingActivitiesWithGLD() {
        // Search for Lake District SOTA references
        ActivitySearchResult result = activityService.getMatchingActivities("G/LD");
        
        // Should find results
        assertTrue(result.hasMatches(), "Expected to find matching activities for G/LD");
        assertFalse(result.getMatches().isEmpty(), "Expected at least some matching activities");
        
        // Verify that at least some results are actual G/LD references
        long gldReferences = result.getMatches().stream()
            .filter(activity -> activity.getRef().startsWith("G/LD-"))
            .count();
        
        assertTrue(gldReferences > 50, 
            String.format("Expected at least 50 G/LD references, but found %d", gldReferences));
        
        // Verify specific known references are found
        boolean foundGLD001 = result.getMatches().stream()
            .anyMatch(activity -> "G/LD-001".equals(activity.getRef()));
        assertTrue(foundGLD001, "Expected to find G/LD-001 (Scafell Pike)");
        
        boolean foundGLD003 = result.getMatches().stream()
            .anyMatch(activity -> "G/LD-003".equals(activity.getRef()));
        assertTrue(foundGLD003, "Expected to find G/LD-003 (Helvellyn)");
    }

    @Test
    public void testGetMatchingActivitiesWithNameSearch() throws Exception {
        // Search for activities by name containing "fell" (common in Lake District)
        ActivitySearchResult result = activityService.getMatchingActivities("fell");
        
        // Should find some results
        assertTrue(result.hasMatches(), "Expected to find matching activities for 'fell'");
        assertFalse(result.getMatches().isEmpty(), "Expected at least some matching activities");
        
        // Verify that results contain activities with "fell" in the name
        boolean foundFellInName = result.getMatches().stream()
            .anyMatch(activity -> activity.getName().toLowerCase().contains("fell"));
        assertTrue(foundFellInName, "Expected to find at least one activity with 'fell' in the name");
    }

    @Test
    public void testGetMatchingActivitiesNoResults() throws Exception {
        // Search for something that should not match
        ActivitySearchResult result = activityService.getMatchingActivities("ZZZZNONEXISTENT");
        
        // Should return valid result but with no matches
        assertNotNull(result, "Expected result object even for no matches");
        assertTrue(result.getMatches().isEmpty(), "Expected no matches for non-existent substring");
    }
}