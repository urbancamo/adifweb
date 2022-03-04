package uk.m0nom.adifweb.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.m0nom.coords.GlobalCoords3D;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LocationSearchResult {
    private GlobalCoords3D coordinates;
    private String info = "";
    private String error = "";
    private Collection<String> matches;

    public boolean hasMatches() {
        return matches != null;
    }
}
