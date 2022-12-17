package uk.m0nom.adifweb.util;

import uk.m0nom.adifproc.activity.ActivityType;
import uk.m0nom.adifproc.adif3.control.TransformControl;
import uk.m0nom.adifproc.icons.IconResource;
import uk.m0nom.adifweb.ApplicationConfiguration;
import uk.m0nom.adifweb.domain.HtmlParameterType;
import uk.m0nom.adifweb.domain.HtmlParameters;

public class TransformControlUtils {
    public static TransformControl createTransformControlFromParameters(ApplicationConfiguration configuration, HtmlParameters parameters) {
        TransformControl control = new TransformControl();
        control.setFormattedOutput(true);
        control.setQslLabels(true);
        control.setGenerateKml(true);

        control.setKmlS2s(true);
        for (ActivityType activity : ActivityType.values()) {
            String ref = String.format("%sRef", activity.getActivityName().toLowerCase());
            if (parameters.get(ref) != null) {
                control.setActivityRef(activity, parameters.get(ref).getValue());
            }
        }

        control.setSatelliteName(parameters.get(HtmlParameterType.SATELLITE_NAME.getParameterName()).getValue());
        control.setSatelliteMode(parameters.get(HtmlParameterType.SATELLITE_MODE.getParameterName()).getValue());
        control.setSatelliteBand(parameters.get(HtmlParameterType.SATELLITE_BAND.getParameterName()).getValue());
        control.setSotaMicrowaveAwardComment(parameters.get(HtmlParameterType.SOTA_MICROWAVE_AWARD_COMMENT.getParameterName()).getValue() != null);
        control.setStripComment(parameters.get(HtmlParameterType.STRIP_COMMENT.getParameterName()).getValue() != null);

        control.setContestResults(parameters.get(HtmlParameterType.CONTEST_RESULTS.getParameterName()).getValue() != null);

        control.setLocation(parameters.get(HtmlParameterType.LOCATION.getParameterName()).getValue());
        control.setEncoding(parameters.get(HtmlParameterType.ENCODING.getParameterName()).getValue());

        control.setKmlContactWidth(3);
        control.setKmlContactTransparency(20);
        control.setKmlContactColourByBand(false);
        control.setKmlContactShadow(true);
        control.setKmlS2sContactLineStyle("brick_red:50:2");
        control.setKmlInternetContactLineStyle("cadmium_yellow:50:1");
        control.setKmlSatelliteTrackLineStyle("brick_red:50:2");

        control.setKmlContactLineStyle("baby_blue:50:2");
        control.setIcon(IconResource.FIXED_ICON_NAME, IconResource.FIXED_DEFAULT_ICON_URL);
        control.setIcon(IconResource.PORTABLE_ICON_NAME, IconResource.PORTABLE_DEFAULT_ICON_URL);
        control.setIcon(IconResource.MOBILE_ICON_NAME, IconResource.MOBILE_DEFAULT_ICON_URL);
        control.setIcon(IconResource.MARITIME_MOBILE_ICON_NAME, IconResource.MARITIME_DEFAULT_ICON_URL);
        control.setIcon(IconResource.AERONAUTICAL_MOBILE_ICON_NAME, IconResource.AERONAUTICAL_DEFAULT_ICON_URL);

        control.setIcon(ActivityType.POTA.getActivityName(), IconResource.POTA_DEFAULT_ICON_URL);
        control.setIcon(ActivityType.SOTA.getActivityName(), IconResource.SOTA_DEFAULT_ICON_URL);
        control.setIcon(ActivityType.GMA.getActivityName(), IconResource.GMA_DEFAULT_ICON_URL);
        control.setIcon(ActivityType.HEMA.getActivityName(), IconResource.HEMA_DEFAULT_ICON_URL);
        control.setIcon(ActivityType.WOTA.getActivityName(), IconResource.WOTA_DEFAULT_ICON_URL);
        control.setIcon(ActivityType.WWFF.getActivityName(), IconResource.WWFF_DEFAULT_ICON_URL);
        control.setIcon(ActivityType.COTA.getActivityName(), IconResource.COTA_DEFAULT_ICON_URL);
        control.setIcon(ActivityType.LOTA.getActivityName(), IconResource.LOTA_DEFAULT_ICON_URL);
        control.setIcon(ActivityType.ROTA.getActivityName(), IconResource.ROTA_DEFAULT_ICON_URL);
        control.setIcon(ActivityType.IOTA.getActivityName(), IconResource.IOTA_DEFAULT_ICON_URL);

        control.setIcon(IconResource.CW_ICON_NAME, IconResource.CW_DEFAULT_ICON_URL);

        control.setIcon(IconResource.SATELLITE_ICON_NAME, IconResource.SATELLITE_DEFAULT_ICON_URL);
        control.setIcon(IconResource.SATELLITE_TRACK_ICON_NAME, IconResource.SATELLITE_TRACK_DEFAULT_ICON_URL);

        control.setKmlShowStationSubLabel(null != parameters.get(HtmlParameterType.STATION_SUBLABEL.getParameterName()).getValue());
        control.setKmlShowActivitySubLabel(null != parameters.get(HtmlParameterType.ACTIVITY_SUBLABEL.getParameterName()).getValue());
        control.setKmlShowLocalActivationSites(parameters.get(HtmlParameterType.LOCAL_ACTIVATION_SITES.getParameterName()).getValue() != null);
        control.setKmlLocalActivationSitesRadius(Double.valueOf(parameters.get(HtmlParameterType.LOCAL_ACTIVATION_SITES_RADIUS.getParameterName()).getValue()));
        control.setAntenna(configuration.getAntennaService().getAntenna(parameters.get(HtmlParameterType.ANTENNA.getParameterName()).getValue()));

        control.setQrzUsername(configuration.getQrzUsername());
        control.setQrzPassword(configuration.getQrzPassword());
        control.setPrintConfigFile(parameters.get(HtmlParameterType.PRINTER_CONFIG.getParameterName()).getValue());
        control.setDontQslCallsigns(parameters.get(HtmlParameterType.DONT_QSL_CALLSIGNS.getParameterName()).getValue());
        control.setQslLabelsInitialPosition(Integer.valueOf(parameters.get(HtmlParameterType.QSL_LABELS_INITIAL_POSITION.getParameterName()).getValue()));
        return control;
    }
}
