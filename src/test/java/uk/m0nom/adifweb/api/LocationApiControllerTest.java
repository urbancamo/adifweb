package uk.m0nom.adifweb.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.m0nom.adifweb.domain.LocationSearchResult;
import uk.m0nom.adifweb.location.LocationService;
import uk.m0nom.coords.GlobalCoords3D;
import uk.m0nom.coords.LocationAccuracy;
import uk.m0nom.coords.LocationSource;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationApiControllerTest {

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationApiController locationApiController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationApiController).build();
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void getGummersHowUppercase() throws Exception {
        GlobalCoords3D coords = new GlobalCoords3D(54.28836000000001, -2.937909999999988, null, LocationSource.ACTIVITY, LocationAccuracy.LAT_LONG);
        String info = "Activity reference match for Summits on the Air location 'Gummer's How'";
        String error = "";
        Collection<String> matches = new ArrayList<>();
        matches.add("54.288360 -2.937910");
        matches.add("54.288360, -2.937910");
        matches.add("54.288360 N 2.937910 W");
        matches.add("54.288360 N, 2.937910 W");
        matches.add("54° 17.302', -2° 56.275'");
        matches.add("54° 17.302' N, 2° 56.275' W");
        matches.add("54° 17' 18\", -2° 56' 16\"");
        matches.add("54° 17' 18\" N, 2° 56' 16\" W");
        matches.add("54° 17' 18.096\"N 2° 56' 16.476\"W");
        matches.add("54° 17' N, 2° 56' W");
        matches.add("IO84MG79KE");
        matches.add("IO84MG79");
        matches.add("IO84MG");
        matches.add("IO84");
        matches.add("E 338953 N 488504");
        matches.add("SD 38952 88504");
        matches.add("SD 3895 8850");
        matches.add("SD 389 288");

        LocationSearchResult gummersHowSearchResult = new LocationSearchResult(coords, info, error, matches);
        when(locationService.getLocation("G/LD-050")).thenReturn(gummersHowSearchResult);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/location")
                        .param("code", "G/LD-050")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(gummersHowSearchResult)))
                .andDo(MockMvcResultHandlers.print());
        verify(locationService, times(1)).getLocation("G/LD-050");
    }

    @Test
    public void getUnknownLocation() throws Exception {
        LocationSearchResult notFound = new LocationSearchResult();
        when(locationService.getLocation("BADLOCATIONREQUEST")).thenReturn(notFound);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/location")
                        .param("code", "BADLOCATIONREQUEST"))
                .andExpect(MockMvcResultMatchers.status().isNotFound()).
                        andDo(MockMvcResultHandlers.print());;
        verify(locationService, times(1)).getLocation("BADLOCATIONREQUEST");
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


