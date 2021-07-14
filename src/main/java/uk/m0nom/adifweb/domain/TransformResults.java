package uk.m0nom.adifweb.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class TransformResults {
    private String adiFile;
    private String kmlFile;
    private String markdownFile;
    private String log;
    private Collection<String> contactsWithoutLocation;
}
