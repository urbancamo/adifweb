package uk.m0nom.adifweb.validation;

import org.apache.commons.lang.StringUtils;
import org.marsik.ham.adif.types.Pota;
import org.marsik.ham.adif.types.PotaList;
import uk.m0nom.adifproc.activity.ActivityDatabaseService;
import uk.m0nom.adifproc.activity.ActivityType;

public class PotaValidator implements Validator {
    private final ActivityDatabaseService databases;

    public PotaValidator(ActivityDatabaseService databases) {
        this.databases = databases;
    }

    @Override
    public ValidationResult isValid(String value) {
        if (StringUtils.isEmpty(value)) {
            return ValidationResult.SUCCESS;
        }
        try {
            PotaList list = PotaList.valueOf(value);
            for (Pota pota : list.getPotaList()) {
                if (databases.getDatabase(ActivityType.POTA).get(pota.getValue()) == null) {
                    String invalidActivityReferenceMessage = "Invalid POTA reference %s";
                    return new ValidationResult(String.format(invalidActivityReferenceMessage, pota.getValue()));
                }
            }
            return ValidationResult.SUCCESS;
        } catch (Exception e) {
            String invalidListofPotas = "POTA field must be one or more references, separate with commas";
            return new ValidationResult(invalidListofPotas);
        }
    }
}
