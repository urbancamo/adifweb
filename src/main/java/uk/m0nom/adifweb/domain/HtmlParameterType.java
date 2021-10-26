package uk.m0nom.adifweb.domain;

import lombok.Getter;

@Getter
public enum HtmlParameterType {
    FILENAME("filename"),
    LATITUDE("latitude"),
    LONGITUDE("longitude"),
    LATLONG("latlong"),
    SOTA_REF("sotaRef"),
    WOTA_REF("wotaRef"),
    HEMA_REF("hemaRef"),
    POTA_REF("potaRef"),
    WWFF_REF("wwffRef"),
    COTA_REF("cotaRef"),
    LOTA_REF("lotaRef"),
    ROTA_REF("rotaRef"),
    GRID("grid"),
    ENCODING("encoding"),
    STATION_SUBLABEL("stationSubLabel"),
    LOCAL_ACTIVATION_SITES("localActivationSites"),
    LOCAL_ACTIVATION_SITES_RADIUS("localActivationSitesRadius"),
    ANTENNA_TAKEOFF_ANGLE("hfAntennaTakeoffAngle"),
    CONTEST_RESULTS("contestResults"),
    SATELLITE_NAME("satName"),
    SATELLITE_MODE("satMode"),
    SATELLITE_BAND("satBand"),
    SOTA_MICROWAVE_AWARD_COMMENT("sotaMicrowaveAwardComment"),
    STRIP_COMMENT("stripComment"),
    PRINTER_CONFIG("printerConfig");

    private String parameterName;

    HtmlParameterType(String parameterName) {
        this.parameterName = parameterName;
    }
}
