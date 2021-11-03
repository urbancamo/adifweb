package uk.m0nom.adifweb.validation;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import uk.m0nom.activity.ActivityDatabases;
import uk.m0nom.activity.ActivityType;

@Getter
@Setter
public class ActivityValidator implements Validator {
    private String invalidActivityReferenceMessage = "Invalid %s reference";

    private ActivityDatabases databases;
    private ActivityType activityType;

    public ActivityValidator(ActivityDatabases databases, ActivityType activityType) {
        this.databases = databases;
        this.activityType = activityType;
    }

    @Override
    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.EMPTY;

        } else if (databases.getDatabase(activityType).get(value) != null) {
            return ValidationResult.SUCCESS;
        } else {
            return new ValidationResult(String.format(invalidActivityReferenceMessage, activityType.getActivityName()));
        }
    }
}
