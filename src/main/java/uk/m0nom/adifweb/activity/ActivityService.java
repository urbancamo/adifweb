package uk.m0nom.adifweb.activity;

import org.springframework.stereotype.Service;
import uk.m0nom.adifproc.activity.Activity;
import uk.m0nom.adifproc.activity.ActivityDatabase;
import uk.m0nom.adifproc.activity.ActivityDatabaseService;
import uk.m0nom.adifproc.activity.ActivityLuceneIndex;
import uk.m0nom.adifproc.activity.ActivityType;
import uk.m0nom.adifweb.ApplicationConfiguration;
import uk.m0nom.adifweb.domain.ActivitySearchResult;

import java.util.*;
import java.util.logging.Logger;

@Service
public class ActivityService {
    private static final Logger logger = Logger.getLogger(ActivityService.class.getName());
    
    private final ActivityDatabaseService activityDatabaseService;
    private final Map<ActivityType, ActivityLuceneIndex> luceneIndexes = new HashMap<>();

    public ActivityService(ApplicationConfiguration applicationConfiguration) {
        this.activityDatabaseService = applicationConfiguration.getActivityDatabases();
    }

    /**
     * Initialize Lucene indexes when data is available.
     * This is called lazily on first search if indexes haven't been created yet.
     */
    private void initializeLuceneIndexes() {
        if (!luceneIndexes.isEmpty()) {
            return; // Already initialized
        }
        
        try {
            logger.info("Initializing Lucene indexes for activity search...");
            
            // Ensure activity databases are loaded first
            if (activityDatabaseService != null) {
                // Create Lucene indexes for each activity type
                for (ActivityType activityType : ActivityType.values()) {
                    try {
                        ActivityDatabase database = activityDatabaseService.getDatabase(activityType);
                        if (database != null && database.size() > 0) {
                            List<Activity> activities = new ArrayList<>(database.getValues());
                            ActivityLuceneIndex luceneIndex = new ActivityLuceneIndex(activities);
                            luceneIndexes.put(activityType, luceneIndex);
                            logger.info(String.format("Created Lucene index for %s with %d activities", 
                                activityType.getActivityName(), activities.size()));
                        }
                    } catch (Exception e) {
                        logger.warning(String.format("Failed to create Lucene index for %s: %s", 
                            activityType.getActivityName(), e.getMessage()));
                        // Continue with other activity types
                    }
                }
                
                logger.info(String.format("Lucene indexes initialized for %d activity types", luceneIndexes.size()));
            } else {
                logger.warning("ActivityDatabaseService is null, skipping Lucene index initialization");
            }
        } catch (Exception e) {
            logger.severe(String.format("Failed to initialize Lucene indexes: %s", e.getMessage()));
            // Don't throw exception - gracefully degrade to empty indexes
            logger.warning("ActivityService will use fallback search without Lucene indexes");
        }
    }

    public ActivitySearchResult getMatchingActivities(String substring) {
        try {
            // Initialize Lucene indexes lazily if not already done
            initializeLuceneIndexes();
            
            // If Lucene indexes are available, use them
            if (!luceneIndexes.isEmpty()) {
                return searchWithLuceneIndexes(substring);
            } else {
                // Fallback to basic pattern matching
                logger.info("Using fallback search as Lucene indexes are not available");
                return searchWithFallbackMethod(substring);
            }
        } catch (Exception e) {
            logger.severe(String.format("Error searching activities with substring '%s': %s", substring, e.getMessage()));
            // Final fallback to empty result on error
            return new ActivitySearchResult(new ArrayList<>());
        }
    }
    
    private ActivitySearchResult searchWithLuceneIndexes(String substring) throws Exception {
        List<Activity> allMatches = new ArrayList<>();
        
        // Search across all activity type indexes
        for (Map.Entry<ActivityType, ActivityLuceneIndex> entry : luceneIndexes.entrySet()) {
            ActivityType activityType = entry.getKey();
            ActivityLuceneIndex luceneIndex = entry.getValue();
            
            // Search by both reference and name
            List<String> refMatches = luceneIndex.searchByRefSubstring(substring);
            List<String> nameMatches = luceneIndex.searchByNameSubstring(substring);
            
            // Combine and deduplicate results
            List<String> combinedRefs = new ArrayList<>(refMatches);
            nameMatches.stream()
                .filter(ref -> !combinedRefs.contains(ref))
                .forEach(combinedRefs::add);
            
            // Convert references back to Activity objects
            ActivityDatabase database = activityDatabaseService.getDatabase(activityType);
            List<Activity> activities = combinedRefs.stream()
                .map(database::get)
                .filter(Objects::nonNull)
                .toList();
            
            allMatches.addAll(activities);
        }
        
        logger.info(String.format("Found %d matching activities for substring: %s (Lucene)", allMatches.size(), substring));
        return new ActivitySearchResult(allMatches);
    }
    
    private ActivitySearchResult searchWithFallbackMethod(String substring) {
        // Basic pattern matching as fallback (original implementation)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i).*" + java.util.regex.Pattern.quote(substring) + ".*");
        List<Activity> matches = activityDatabaseService.findAllActivities().stream()
                .filter(activity -> pattern.matcher(activity.getName()).matches() || 
                                  pattern.matcher(activity.getRef()).matches())
                .toList();
        
        logger.info(String.format("Found %d matching activities for substring: %s (fallback)", matches.size(), substring));
        return new ActivitySearchResult(matches);
    }
}