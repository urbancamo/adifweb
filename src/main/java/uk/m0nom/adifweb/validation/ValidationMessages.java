package uk.m0nom.adifweb.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@PropertySource("validation-messages.properties")
public class ValidationMessages {
    @Autowired
    Environment env;

    private static ValidationMessages instance;

    public static ValidationMessages getInstance() {return instance;}

    public String getMessage(String prop) {
        return env.getProperty(prop);
    }
}
