package uk.m0nom.adifweb.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.m0nom.adifproc.activity.Activity;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySearchResult {
    private Collection<Activity> matches;

    public boolean hasMatches() {
        return matches != null;
    }
}
